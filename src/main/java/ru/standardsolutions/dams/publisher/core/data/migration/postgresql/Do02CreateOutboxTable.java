package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.standardsolutions.dams.publisher.core.data.DatabaseVoidOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

record Do02CreateOutboxTable(Connection connection) implements DatabaseVoidOperation {

    private static final Logger logger = LoggerFactory.getLogger(Do02CreateOutboxTable.class);

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
                logger.debug("Migration {} already applied, skipping", migrationId);
                return;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check changelog table", e);
        }
        logger.info("Attempting to create table: {} in schema: {}", tableName, schema);



        boolean originalAutoCommit = false;
        boolean transactionStarted = false;

        try {
            // Сохраняем и отключаем autoCommit
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            transactionStarted = true;

            // Выполняем все SQL в одном блоке
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("""
                BEGIN;
                
                CREATE TABLE IF NOT EXISTS outbox_message (
                    id UUID PRIMARY KEY,
                    payload TEXT NOT NULL,
                    type VARCHAR(255) NOT NULL,
                    recipient VARCHAR(255) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    processed_at TIMESTAMPTZ
                );
                
                CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed_recipient_type
                ON outbox_message (recipient, type)
                WHERE processed_at IS NULL;
                
                CREATE INDEX IF NOT EXISTS idx_outbox_processed_at
                ON outbox_message (processed_at);
                
                COMMIT;
                """);

                logger.info("Outbox tables and indexes created successfully");
            }

        } catch (SQLException e) {
            try {
                if (transactionStarted && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                logger.error("Rollback failed", rollbackEx);
                e.addSuppressed(rollbackEx);
            }
            throw new RuntimeException("Outbox migration failed", e);

        } finally {
            if (connection != null) {
                try {
                    if (transactionStarted) {
                        connection.setAutoCommit(originalAutoCommit);
                    }
                } catch (SQLException e) {
                    logger.error("Failed to restore auto-commit", e);
                }
            }
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
