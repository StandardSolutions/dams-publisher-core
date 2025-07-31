package ru.standardsolutions.dams.publisher.core.initializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Класс для инициализации схемы outbox в базе данных.
 * Поддерживает PostgreSQL, MySQL, SQL Server, Oracle, H2.
 * Выполняет SQL скрипт создания таблицы и индексов.
 * Поддерживает проверку версии и блокировку для предотвращения одновременного выполнения.
 */
public class SchemaInitializer {

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
     * Выполняет инициализацию схемы outbox с проверкой версии и блокировкой
     */
    public void initializeSchema() throws SQLException, IOException {
        System.out.println("Начинаем инициализацию схемы outbox...");
        System.out.println("Instance ID: " + instanceId);
        System.out.println("Database Type: " + databaseType);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            
            // Проверяем, нужна ли инициализация
            if (isSchemaUpToDate(connection)) {
                System.out.println("✅ Схема уже актуальна (версия " + CURRENT_VERSION + "), инициализация не требуется");
                return;
            }
            
            // Пытаемся получить блокировку
            if (!acquireLock(connection)) {
                System.out.println("⏳ Другой экземпляр выполняет инициализацию, ожидаем...");
                waitForInitialization(connection);
                return;
            }
            
            try {
                // Выполняем инициализацию
                String sqlScript = loadSchemaScript();
                executeSchemaScript(connection, sqlScript);
                System.out.println("✅ Схема outbox успешно инициализирована экземпляром " + instanceId);
                
            } finally {
                // Освобождаем блокировку
                releaseLock(connection);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка при инициализации схемы outbox: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Проверяет, актуальна ли схема
     */
    private boolean isSchemaUpToDate(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT 1 FROM schema_version WHERE version = ?")) {
            stmt.setString(1, CURRENT_VERSION);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Схема актуальна, если версия найдена
            }
        } catch (SQLException e) {
            // Таблица schema_version не существует - нужна инициализация
            return false;
        }
    }

    /**
     * Пытается получить блокировку для инициализации
     */
    private boolean acquireLock(Connection connection) throws SQLException {
        // Создаем таблицу блокировки, если её нет
        createLockTableIfNotExists(connection);
        
        String insertSql = getLockSql("insert");
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setString(1, instanceId);
            stmt.executeUpdate();
            System.out.println("🔒 Блокировка получена экземпляром " + instanceId);
            return true;
        } catch (SQLException e) {
            // Блокировка уже занята другим экземпляром
            return false;
        }
    }

    /**
     * Создает таблицу блокировки с учетом типа СУБД
     */
    private void createLockTableIfNotExists(Connection connection) throws SQLException {
        String createLockTable = loadCreateLockTableScript();
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createLockTable);
        }
    }

    /**
     * Загружает SQL скрипт для создания таблицы блокировки
     */
    private String loadCreateLockTableScript() {
        try {
            String scriptPath = "/db/scripts/create_lock_table_" + databaseType.name().toLowerCase() + ".sql";
            return loadScript(scriptPath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить SQL скрипт создания таблицы блокировки", e);
        }
    }

    /**
     * Освобождает блокировку
     */
    private void releaseLock(Connection connection) throws SQLException {
        String deleteSql = getLockSql("delete");
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setString(1, instanceId);
            stmt.executeUpdate();
            System.out.println("🔓 Блокировка освобождена экземпляром " + instanceId);
        }
    }

    /**
     * Ожидает завершения инициализации другим экземпляром
     */
    private void waitForInitialization(Connection connection) throws SQLException {
        int maxWaitSeconds = 60;
        int waitIntervalMs = 1000;
        
        for (int i = 0; i < maxWaitSeconds; i++) {
            try {
                Thread.sleep(waitIntervalMs);
                
                // Проверяем, завершилась ли инициализация
                if (isSchemaUpToDate(connection)) {
                    System.out.println("✅ Инициализация завершена другим экземпляром");
                    return;
                }
                
                // Проверяем, не зависла ли блокировка (старше 5 минут)
                if (isLockStale(connection)) {
                    System.out.println("⚠️ Обнаружена зависшая блокировка, очищаем...");
                    clearStaleLock(connection);
                    return;
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("Ожидание прервано", e);
            }
        }
        
        throw new SQLException("Таймаут ожидания инициализации (60 секунд)");
    }

    /**
     * Проверяет, не зависла ли блокировка
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
     * Очищает зависшую блокировку
     */
    private void clearStaleLock(Connection connection) throws SQLException {
        String clearStaleSql = getLockSql("clear_stale");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(clearStaleSql);
        }
    }

    /**
     * Загружает SQL для операций с блокировкой из файла
     */
    private String getLockSql(String operation) {
        String cacheKey = databaseType.name().toLowerCase() + "_" + operation;
        
        if (!lockSqlCache.containsKey(cacheKey)) {
            try {
                String scriptPath = "/db/scripts/lock_" + databaseType.name().toLowerCase() + ".sql";
                String script = loadScript(scriptPath);
                parseLockScript(script, lockSqlCache);
            } catch (IOException e) {
                throw new RuntimeException("Не удалось загрузить SQL скрипт блокировки", e);
            }
        }
        
        return lockSqlCache.get(cacheKey);
    }

    /**
     * Парсит SQL скрипт блокировки и извлекает нужные запросы
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
        
        // Добавляем последнюю операцию
        if (currentOperation != null) {
            cache.put(databaseType.name().toLowerCase() + "_" + currentOperation, currentSql.toString().trim());
        }
    }

    /**
     * Загружает SQL скрипт из ресурсов приложения
     */
    private String loadSchemaScript() throws IOException {
        String scriptPath = "/db/scripts/outbox_" + databaseType.name().toLowerCase() + "_schema.sql";
        return loadScript(scriptPath);
    }

    /**
     * Загружает скрипт из ресурсов
     */
    private String loadScript(String scriptPath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(scriptPath)) {
            if (inputStream == null) {
                throw new IOException("Не удалось найти файл скрипта: " + scriptPath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Выполняет SQL скрипт создания схемы
     */
    private void executeSchemaScript(Connection connection, String sqlScript) throws SQLException {
        // Отключаем автокоммит для транзакционного выполнения
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        
        try {
            // Выполняем весь скрипт как одну транзакцию
            try (Statement statement = connection.createStatement()) {
                statement.execute(sqlScript);
                System.out.println("SQL скрипт выполнен успешно");
            }
            
            // Коммитим транзакцию
            connection.commit();
            System.out.println("✅ Транзакция зафиксирована - все изменения применены");
            
        } catch (SQLException e) {
            // Откатываем транзакцию при ошибке
            try {
                connection.rollback();
                System.err.println("❌ Транзакция откачена из-за ошибки: " + e.getMessage());
                System.err.println("🔄 Все изменения отменены - база данных осталась в исходном состоянии");
            } catch (SQLException rollbackEx) {
                System.err.println("⚠️ Ошибка при откате транзакции: " + rollbackEx.getMessage());
            }
            throw e;
        } finally {
            // Восстанавливаем исходное состояние автокоммита
            connection.setAutoCommit(autoCommit);
        }
    }
}