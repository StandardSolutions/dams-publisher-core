package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.standardsolutions.dams.publisher.core.data.DatabaseVoidOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

record Do01CreateRecipientTable(Connection connection) implements DatabaseVoidOperation {

    private static final Logger logger = LoggerFactory.getLogger(Do01CreateRecipientTable.class);

    @Override
    public void execute(String... args) {
        String schema = args[0];
        String tableName = args[1];
        String changelogTable = args.length > 2 ? args[2] : "dams_changelog";
        String migrationId = this.getClass().getSimpleName();

        // Проверяем, выполнялась ли миграция
        String sqlCheckMigration = "SELECT 1 FROM " + changelogTable + " WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sqlCheckMigration)) {
            ps.setString(1, migrationId);
            if (ps.executeQuery().next()) {
                System.out.println("Migration '" + migrationId + "' already applied, skipping");
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check changelog table", e);
        }
        String fullTableName = schema + "." + tableName;
        String sqlCreateTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    id VARCHAR(255) PRIMARY KEY,
                    channel_type VARCHAR(64) NOT NULL,
                    connection_params TEXT NOT NULL,
                    description VARCHAR(255),
                    enabled BOOLEAN default false,
                    executed_date TIMESTAMP NOT NULL DEFAULT NOW()
                )
                """.formatted(fullTableName);
        logger.info("Attempting to create table: {} in schema: {}", tableName, schema);
        logger.info("SQL for recipient table: {}", sqlCreateTable);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlCreateTable);
            logger.info("Executed query={}", this.getClass().getName());
        } catch (Exception e) {
            throw new RuntimeException("Create table Exception" + tableName, e);
        }

        // Записываем факт выполнения миграции
        String sqlInsertChangelog = "INSERT INTO " + changelogTable + " (id, description, executed_date) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = connection.prepareStatement(sqlInsertChangelog)) {
            ps.setString(1, migrationId);
            ps.setString(2, "Create recipient table");
            ps.executeUpdate();
            logger.info("Migration '{}' recorded in changelog", migrationId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert changelog record", e);
        }
    }
}
