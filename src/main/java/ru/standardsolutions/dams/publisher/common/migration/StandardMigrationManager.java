package ru.standardsolutions.dams.publisher.common.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.standardsolutions.dams.publisher.common.MigrationLog;
import ru.standardsolutions.dams.publisher.common.MigrationManager;
import ru.standardsolutions.dams.publisher.common.database.DataSourceException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class StandardMigrationManager implements MigrationManager {

    private static final Logger logger = LoggerFactory.getLogger(StandardMigrationManager.class);

    private final DataSource dataSource;

    public StandardMigrationManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void execute(MigrationLog migrationLog) {

        try (Connection connection = dataSource.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            boolean supportsTransactions = connection.getMetaData().supportsTransactions();

            try {
                if (supportsTransactions && originalAutoCommit) {
                    connection.setAutoCommit(false); // Включаем ручное управление транзакцией
                }

                for (var migration : migrationLog.actions()) {
                    migration.execute(connection);
                }

                if (supportsTransactions && originalAutoCommit) {
                    connection.commit(); // Коммитим, только если мы реально управляли транзакцией
                }
            } catch (SQLException e) {
                if (supportsTransactions && originalAutoCommit && !connection.isClosed()) {
                    try {
                        connection.rollback(); // Откатываем, если транзакции поддерживаются и мы их контролировали
                    } catch (SQLException rollbackEx) {
                        logger.error("Transaction rollback failed", rollbackEx);
                        e.addSuppressed(rollbackEx);
                    }
                }
                throw new DataSourceException("Transaction failed", e);
            } finally {
                if (supportsTransactions && originalAutoCommit && !connection.isClosed()) {
                    try {
                        connection.setAutoCommit(true); // Восстанавливаем auto-commit только если меняли
                    } catch (SQLException e) {
                        logger.warn("Failed to restore auto-commit state", e);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataSourceException("Failed to obtain connection", e);
        }
    }
}