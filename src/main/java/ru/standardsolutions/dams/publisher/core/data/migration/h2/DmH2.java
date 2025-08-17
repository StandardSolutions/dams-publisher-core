package ru.standardsolutions.dams.publisher.core.data.migration.h2;

import ru.standardsolutions.dams.publisher.core.data.database.DataSourceException;
import ru.standardsolutions.dams.publisher.core.data.migration.MigrationManager;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public record DmH2() implements MigrationManager {
    @Override
    public void execute() {
        // Этот метод не использует DataSource, так как DmH2 создается без параметров
        // В реальном приложении здесь должна быть логика для получения DataSource
        throw new UnsupportedOperationException("DmH2 requires DataSource to be injected");
    }
    
    public void execute(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            // Создаем таблицу блокировки
            createLockTable(connection);
            
            // Создаем таблицу changelog
            createChangelogTable(connection);
            
            // Создаем outbox таблицы
            createOutboxTables(connection);
            
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }
    
    private void createLockTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS schema_initialization_lock (
                    instance_id VARCHAR(255) PRIMARY KEY,
                    acquired_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP()
                )
                """);
        }
    }
    
    private void createChangelogTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS dams_changelog_table (
                    id IDENTITY PRIMARY KEY,
                    version VARCHAR(50) NOT NULL,
                    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                    description VARCHAR(1000)
                )
                """);
        }
    }
    
    private void createOutboxTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            // Создаем таблицу версий схемы
            statement.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    id IDENTITY PRIMARY KEY,
                    version VARCHAR(50) NOT NULL,
                    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                    description VARCHAR(1000)
                )
                """);
            
            // Создаем таблицу outbox
            statement.execute("""
                CREATE TABLE IF NOT EXISTS outbox_message (
                    id VARCHAR(36) PRIMARY KEY,
                    payload CLOB NOT NULL,
                    type VARCHAR(255) NOT NULL,
                    recipient VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                    processed_at TIMESTAMP
                )
                """);
            
            // Создаем индексы
            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed_recipient_type
                ON outbox_message (recipient, type)
                """);
            
            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_outbox_processed_at
                ON outbox_message (processed_at)
                """);
        }
    }
}
