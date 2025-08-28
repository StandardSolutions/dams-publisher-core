package ru.standardsolutions.dams.publisher.core.migration.postgresql;

import ru.standardsolutions.dams.publisher.common.MigrationStep;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import java.sql.Connection;
import java.sql.SQLException;

final class Ma02CreateOutboxTable implements MigrationStep {

    private final DamsOptions options;

    public Ma02CreateOutboxTable(DamsOptions options) {
        this.options = options;
    }

    @Override
    public String id() {
        return getClass().getSimpleName();
    }

    @Override
    public String description() {
        return "Create outbox table: " + options.schema() + "." + options.tableName("outbox");
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        final String fullTableName = options.tableName("outbox");

        try (var stmt = connection.createStatement()) {
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS %1$s (
                        id UUID PRIMARY KEY,
                        payload TEXT NOT NULL,
                        databaseType VARCHAR(255) NOT NULL,
                        recipient VARCHAR(255) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                        processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
                    )
                    """.formatted(fullTableName));

            stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_%1$s_unprocessed
                    ON %1$s (recipient, databaseType)
                    WHERE processed_at IS NULL
                    """.formatted(fullTableName));

            stmt.execute("""
                    CREATE INDEX IF NOT EXISTS idx_%1$s_processed_at
                    ON %1$s (processed_at)
                    """.formatted(fullTableName));
        }
    }
}
