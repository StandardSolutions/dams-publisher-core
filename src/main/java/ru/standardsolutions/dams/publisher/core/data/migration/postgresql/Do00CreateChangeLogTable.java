package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.standardsolutions.dams.publisher.core.data.DatabaseVoidOperation;

import java.sql.*;

record Do00CreateChangeLogTable(Connection connection) implements DatabaseVoidOperation {

    private static final Logger logger = LoggerFactory.getLogger(Do00CreateChangeLogTable.class);

    @Override
    public void execute(String... args) {
        String schema = args[0];
        String tableName = args[1];
        String sqlIsExistTable = """
                SELECT 1 FROM pg_catalog.pg_class c
                JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = ? AND c.relname = ? LIMIT 1
                """;

        String sqlCreateTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    id VARCHAR(255) PRIMARY KEY,
                    description VARCHAR(255),
                    executed_date TIMESTAMP NOT NULL DEFAULT NOW()
                )
                """.formatted(tableName);
        try (PreparedStatement stmt = connection.prepareStatement(sqlIsExistTable)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            boolean isExist = stmt.executeQuery().next();
            if (isExist) {
                logger.debug("Table={} in schema={} already exist", tableName, schema);
                return;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlCreateTable);
            logger.info("Executed query={}", this.getClass().getName());
        } catch (Exception e) {
            throw new RuntimeException("Create table Exception" + tableName, e);
        }
    }
}
