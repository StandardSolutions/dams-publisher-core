package ru.standardsolutions.dams.publisher.core;

import ru.standardsolutions.dams.publisher.common.MigrationLog;
import ru.standardsolutions.dams.publisher.common.database.DataSourceException;
import ru.standardsolutions.dams.publisher.common.database.JdbcDatabase;
import ru.standardsolutions.dams.publisher.common.migration.StandardMigrationManager;
import ru.standardsolutions.dams.publisher.common.options.DataOptions;
import ru.standardsolutions.dams.publisher.core.migration.postgresql.PostgreSQLMigrationLog;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public record DamsPublisherCore(DataSource dataSource, String... args) {

    public void execute() {
        JdbcDatabase db = new JdbcDatabase(dataSource);
        DataOptions dataOptions = new DataOptions(args);
//        if (db.type() == DbType.H2) {
//            H2MigrationManager h2Manager = new H2MigrationManager();
//            h2Manager.execute(dataSource);
//        } else if (db.type() == DbType.POSTGRESQL) {
//            StandardMigrationManager postgresManager = new StandardMigrationManager(dataSource);
//        } else {
//            throw new UnsupportedOperationException("Unsupported database type: " + db.type());
//        }

        StandardMigrationManager migrationManager = new StandardMigrationManager();
        MigrationLog migrationLog = new PostgreSQLMigrationLog(dataOptions);

        try (Connection connection = dataSource.getConnection()) {
            migrationManager.execute(connection, migrationLog);
        } catch (SQLException e) {
            throw new DataSourceException("Failed to obtain connection", e);
        }

    }
}