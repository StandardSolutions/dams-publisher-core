package ru.standardsolutions.dams.publisher.core;

import ru.standardsolutions.dams.publisher.common.DamsMigration;
import ru.standardsolutions.dams.publisher.common.metadata.DataSourceException;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public record DamsPublisherCore(DataSource dataSource, String... args) {

    public void execute() {
        // Database
        // get migrations
        // MigrationManager execute(changelog)
        DamsMigration migration = new DamsMigration(dataSource, args);

        try {
            migration.init();
        } catch (SQLException | IOException e) {
            throw new DataSourceException("Failed to execute migration", e);
        }

    }
}