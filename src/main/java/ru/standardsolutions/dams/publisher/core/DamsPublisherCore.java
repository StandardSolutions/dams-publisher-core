package ru.standardsolutions.dams.publisher.core;

import ru.standardsolutions.dams.publisher.common.ChangeLog;
import ru.standardsolutions.dams.publisher.common.database.DataSourceException;
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
        PgDatabase db = new PgDatabase(dataSource, null);
        DamsOptions damsOptions = new DamsOptions(args);
//        if (db.type() == DbType.H2) {
//            H2MigrationManager h2Manager = new H2MigrationManager();
//            h2Manager.execute(dataSource);
//        } else if (db.type() == DbType.POSTGRESQL) {
//            StandardMigrationManager postgresManager = new StandardMigrationManager(dataSource);
//        } else {
//            throw new UnsupportedOperationException("Unsupported database type: " + db.type());
//        }

        StandardMigrationManager migrationManager = new StandardMigrationManager();
        ChangeLog changeLog = new PgChangeLog(damsOptions);

        try (Connection connection = dataSource.getConnection()) {
            migrationManager.execute(connection, changeLog);
        } catch (SQLException e) {
            throw new DataSourceException("Failed to obtain connection", e);
        }

    }
}