package ru.standardsolutions.dams.publisher.core.migration.postgresql;

import ru.standardsolutions.dams.publisher.common.MigrationStep;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import java.sql.Connection;
import java.sql.SQLException;

final class Ma01CreateRecipient implements MigrationStep {

    private final DamsOptions options;

    public Ma01CreateRecipient(DamsOptions options) {
        this.options = options;
    }

    @Override
    public String id() {
        return getClass().getSimpleName();
    }

    @Override
    public String description() {
        return "Create recipient table: " + options.schema() + "." + options.tableName("recipient");
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        final String fullTableName = options.tableName("recipient");
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