package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import ru.standardsolutions.dams.publisher.core.data.migration.MigrationManager;

import javax.sql.DataSource;

public record MmPostgreSQL(DataSource dataSource) implements MigrationManager {
    @Override
    public void execute() {
        new DoCreateLockTable();

    }
}
