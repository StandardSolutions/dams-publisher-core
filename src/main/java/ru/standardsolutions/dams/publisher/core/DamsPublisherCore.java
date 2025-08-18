package ru.standardsolutions.dams.publisher.core;

import ru.standardsolutions.dams.publisher.common.database.JdbcDatabase;
import ru.standardsolutions.dams.publisher.common.migration.StandardMigrationManager;
import ru.standardsolutions.dams.publisher.core.migration.postgresql.PostgreSQLMigrationLog;
import ru.standardsolutions.dams.publisher.common.options.DataOptions;

import javax.sql.DataSource;

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

        StandardMigrationManager migrationManager = new StandardMigrationManager(dataSource);
        migrationManager.execute(new PostgreSQLMigrationLog(dataOptions));
    }


}