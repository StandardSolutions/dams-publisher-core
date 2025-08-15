package ru.standardsolutions.dams.publisher.core.data.init;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for initializing outbox schema in database.
 * Supports PostgreSQL, MySQL, SQL Server, Oracle, H2.
 * Executes SQL script for creating tables and indexes.
 * Supports version checking and locking to prevent concurrent execution.
 */
public class SchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SchemaInitializer.class);
    private static final String CURRENT_VERSION = "1.0";

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String instanceId;
    private final DatabaseType databaseType;
    private final Map<String, String> lockSqlCache = new HashMap<>();

    public SchemaInitializer(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.instanceId = UUID.randomUUID().toString();
        this.databaseType = DatabaseType.ofJdbcUrl(jdbcUrl);
    }

    /**
     * Performs outbox schema initialization with version checking and locking
     */
    public void initializeSchema() throws SQLException, IOException {
        logger.info("Starting outbox schema initialization...");
        logger.info("Instance ID: {}", instanceId);
        logger.info("Database Type: {}", databaseType);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            
            // Check if initialization is needed
            if (isSchemaUpToDate(connection)) {
                logger.info("✅ Schema is already up to date (version {}), initialization not required", CURRENT_VERSION);
                return;
            }
            
            // Try to acquire lock
            if (!acquireLock(connection)) {
                logger.info("⏳ Another instance is performing initialization, waiting...");
                waitForInitialization(connection);
                return;
            }
            
            try {
                // Perform initialization
                String sqlScript = loadSchemaScript();
                executeSchemaScript(connection, sqlScript);
                logger.info("✅ Outbox schema successfully initialized by instance {}", instanceId);
                
            } finally {
                // Release lock
                releaseLock(connection);
            }
            
        } catch (Exception e) {
            logger.error("❌ Error during outbox schema initialization: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Checks if schema is up to date
     */
    private boolean isSchemaUpToDate(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM schema_version WHERE version = ?")) {
            stmt.setString(1, CURRENT_VERSION);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Schema is up to date if version is found
            }
        } catch (SQLException e) {
            // schema_version table doesn't exist - initialization needed
            return false;
        }
    }

    /**
     * Attempts to acquire lock for initialization
     */
    private boolean acquireLock(Connection connection) throws SQLException {
        // Create lock table if it doesn't exist
        createLockTableIfNotExists(connection);
        
        String insertSql = getLockSql("insert");
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setString(1, instanceId);
            stmt.executeUpdate();
            logger.info("🔒 Lock acquired by instance {}", instanceId);
            return true;
        } catch (SQLException e) {
            // Lock is already held by another instance
            return false;
        }
    }

    /**
     * Creates lock table considering database type
     */
    private void createLockTableIfNotExists(Connection connection) throws SQLException {
        String createLockTable = loadCreateLockTableScript();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createLockTable);
        }
    }

    /**
     * Loads SQL script for creating lock table
     */
    private String loadCreateLockTableScript() {
        try {
            String scriptPath = "/db/scripts/create_lock_table_" + databaseType.name().toLowerCase() + ".sql";
            return loadScript(scriptPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL script for creating lock table", e);
        }
    }

    /**
     * Releases the lock
     */
    private void releaseLock(Connection connection) throws SQLException {
        String deleteSql = getLockSql("delete");
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setString(1, instanceId);
            stmt.executeUpdate();
            logger.info("🔓 Lock released by instance {}", instanceId);
        }
    }

    /**
     * Waits for initialization completion by another instance
     */
    private void waitForInitialization(Connection connection) throws SQLException {
        int maxWaitSeconds = 60;
        int waitIntervalMs = 1000;
        
        for (int i = 0; i < maxWaitSeconds; i++) {
            try {
                Thread.sleep(waitIntervalMs);
                
                // Check if initialization is completed
                if (isSchemaUpToDate(connection)) {
                    logger.info("✅ Initialization completed by another instance");
                    return;
                }
                
                // Check if lock is stale (older than 5 minutes)
                if (isLockStale(connection)) {
                    logger.warn("⚠️ Stale lock detected, clearing...");
                    clearStaleLock(connection);
                    return;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Waiting interrupted", e);
            }
        }
        
        throw new SQLException("Initialization wait timeout (60 seconds)");
    }

    /**
     * Checks if lock is stale
     */
    private boolean isLockStale(Connection connection) throws SQLException {
        String staleCheckSql = getLockSql("check_stale");
        try (PreparedStatement stmt = connection.prepareStatement(staleCheckSql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Clears stale lock
     */
    private void clearStaleLock(Connection connection) throws SQLException {
        String clearStaleSql = getLockSql("clear_stale");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(clearStaleSql);
        }
    }

    /**
     * Loads SQL for lock operations from file
     */
    private String getLockSql(String operation) {
        String cacheKey = databaseType.name().toLowerCase() + "_" + operation;
        
        if (!lockSqlCache.containsKey(cacheKey)) {
            try {
                String scriptPath = "/db/scripts/lock_" + databaseType.name().toLowerCase() + ".sql";
                String script = loadScript(scriptPath);
                parseLockScript(script, lockSqlCache);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load SQL lock script", e);
            }
        }
        
        return lockSqlCache.get(cacheKey);
    }

    /**
     * Parses SQL lock script and extracts required queries
     */
    private void parseLockScript(String script, Map<String, String> cache) {
        String[] lines = script.split("\n");
        String currentOperation = null;
        StringBuilder currentSql = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("-- Insert lock")) {
                if (currentOperation != null) {
                    cache.put(databaseType.name().toLowerCase() + "_" + currentOperation, currentSql.toString().trim());
                }
                currentOperation = "insert";
                currentSql = new StringBuilder();
            } else if (line.startsWith("-- Delete lock")) {
                if (currentOperation != null) {
                    cache.put(databaseType.name().toLowerCase() + "_" + currentOperation, currentSql.toString().trim());
                }
                currentOperation = "delete";
                currentSql = new StringBuilder();
            } else if (line.startsWith("-- Check for stale locks")) {
                if (currentOperation != null) {
                    cache.put(databaseType.name().toLowerCase() + "_" + currentOperation, currentSql.toString().trim());
                }
                currentOperation = "check_stale";
                currentSql = new StringBuilder();
            } else if (line.startsWith("-- Clear stale locks")) {
                if (currentOperation != null) {
                    cache.put(databaseType.name().toLowerCase() + "_" + currentOperation, currentSql.toString().trim());
                }
                currentOperation = "clear_stale";
                currentSql = new StringBuilder();
            } else if (!line.startsWith("--") && !line.isEmpty() && currentOperation != null) {
                currentSql.append(line).append(" ");
            }
        }
        
        // Add the last operation
        if (currentOperation != null) {
            cache.put(databaseType.name().toLowerCase() + "_" + currentOperation, currentSql.toString().trim());
        }
    }

    /**
     * Loads SQL script from application resources
     */
    private String loadSchemaScript() throws IOException {
        String scriptPath = "/db/scripts/outbox_" + databaseType.name().toLowerCase() + "_schema.sql";
        return loadScript(scriptPath);
    }

    /**
     * Loads script from resources
     */
    private String loadScript(String scriptPath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(scriptPath)) {
            if (inputStream == null) {
                throw new IOException("Failed to find script file: " + scriptPath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Executes SQL schema creation script
     */
    private void executeSchemaScript(Connection connection, String sqlScript) throws SQLException {
        // Disable autocommit for transactional execution
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        
        try {
            // Execute entire script as single transaction
            try (Statement statement = connection.createStatement()) {
                statement.execute(sqlScript);
                logger.info("SQL script executed successfully");
            }
            
            // Commit transaction
            connection.commit();
            logger.info("✅ Transaction committed - all changes applied");
            
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                connection.rollback();
                logger.error("❌ Transaction rolled back due to error: {}", e.getMessage());
                logger.info("🔄 All changes cancelled - database remained in original state");
            } catch (SQLException rollbackEx) {
                logger.error("⚠️ Error during transaction rollback: {}", rollbackEx.getMessage(), rollbackEx);
            }
            throw e;
        } finally {
            // Restore original autocommit state
            connection.setAutoCommit(autoCommit);
        }
    }
}