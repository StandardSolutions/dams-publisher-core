package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import ru.standardsolutions.dams.publisher.core.data.migration.MigrationAction;
import ru.standardsolutions.dams.publisher.core.data.options.DataOptions;

import java.sql.Connection;
import java.sql.SQLException;

final class Ma01CreateRecipient implements MigrationAction {

    private final DataOptions options;

    public Ma01CreateRecipient(DataOptions options) {
        this.options = options;
    }

    @Override
    public String id() {
        return getClass().getSimpleName();
    }

    @Override
    public String description() {
        return "Create recipient table: " + options.schema() + "." + options.recipientTableName();
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        final String fullTableName = options.recipientTableName();
        final String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id VARCHAR(255) PRIMARY KEY,
                    enabled BOOLEAN default false,
                    created_at TIMESTAMP NOT NULL DEFAULT NOW()
                )
                """.formatted(fullTableName);
        try (var stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}