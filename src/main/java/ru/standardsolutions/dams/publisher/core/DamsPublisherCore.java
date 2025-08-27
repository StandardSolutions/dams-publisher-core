package ru.standardsolutions.dams.publisher.core;

import ru.standardsolutions.dams.publisher.common.ChangeLog;
import ru.standardsolutions.dams.publisher.common.DamsMigration;
import ru.standardsolutions.dams.publisher.common.metadata.DataSourceException;
import ru.standardsolutions.dams.publisher.common.migration.postgresql.PgDatabase;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;
import ru.standardsolutions.dams.publisher.common.migration.postgresql.PgChangeLog;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public record DamsPublisherCore(DataSource dataSource, String... args) {

    public void execute() {
        // Database
        // get migrations
        // MigrationManager execute(changelog)
        DamsMigration migration = new DamsMigration(dataSource, args);

        try {
            migration.init();
        } catch (SQLException e) {
            throw new DataSourceException("Failed to execute migration", e);
        }

    }
}