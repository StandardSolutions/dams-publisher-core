package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import ru.standardsolutions.dams.publisher.core.data.database.DataSourceException;
import ru.standardsolutions.dams.publisher.core.data.migration.MigrationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public record MmPostgreSQL(DataSource dataSource) implements MigrationManager {
    @Override
    public void execute() {
        try (Connection connection = dataSource.getConnection()) {
            new DoSetLock(connection).execute();
            
            // Создаем все необходимые таблицы
            createAllTables(connection);
            
            new DoReleaseLock(connection).execute();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }
    
    private void createAllTables(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            
            // Создаем таблицу dams_changelog_table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dams_changelog_table (
                    id SERIAL PRIMARY KEY,
                    version VARCHAR(50) NOT NULL,
                    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    description TEXT
                )
                """);
            
            // Создаем таблицу schema_initialization_lock
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schema_initialization_lock (
                    instance_id VARCHAR(255) PRIMARY KEY,
                    acquired_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                )
                """);
            
            // Создаем таблицу schema_version
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    id SERIAL PRIMARY KEY,
                    version VARCHAR(50) NOT NULL,
                    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    description TEXT
                )
                """);
            
            // Создаем таблицу outbox_message
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS outbox_message (
                    id UUID PRIMARY KEY,
                    payload TEXT NOT NULL,
                    type VARCHAR(255) NOT NULL,
                    recipient VARCHAR(255) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    processed_at TIMESTAMPTZ
                )
                """);
            
            // Создаем индексы
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed_recipient_type
                ON outbox_message (recipient, type)
                WHERE processed_at IS NULL
                """);
            
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_outbox_processed_at
                ON outbox_message (processed_at)
                """);
        }
    }
}