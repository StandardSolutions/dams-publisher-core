package ru.standardsolutions.dams.publisher.core.migration.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.standardsolutions.dams.publisher.common.MigrationStep;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

final class Ma00CreateChangeLogTable implements MigrationStep {

    private static final Logger logger = LoggerFactory.getLogger(Ma00CreateChangeLogTable.class);

    private final DamsOptions options;

    public Ma00CreateChangeLogTable(DamsOptions options) {
        this.options = options;
    }


    @Override
    public String id() {
        return "";
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        final String schema = options.schema();
        final String tableName = options.changeLogTableName();

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
