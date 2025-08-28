package ru.standardsolutions.dams.publisher.common;

import ru.standardsolutions.dams.publisher.common.metadata.DatabaseMetadata;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class DamsMigration {

    private final DataSource dataSource;

    private final String[] args;

    public DamsMigration(final DataSource dataSource, final String... args) {
        this.dataSource = dataSource;
        this.args = args;
    }

    public void init() throws SQLException, IOException {
        DamsOptions options = new DamsOptions(this.args);
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetadata metadata = new DatabaseMetadata(c);
            Database db = metadata.database();
            try (AdvisoryLock lock = db.newLock(c, options)) {
                lock.acquire();

                ChangeLog changeLog = db.changelog();
                changeLog.ensureExist(c);

                MigrationLoader migrationLoader = new MigrationLoader(options);
                for (MigrationStep migration : migrationLoader.steps()) {
                    if (changeLog.has(c, migration)) {
                        continue;
                    }
                    migration.execute(c);
                    changeLog.append(c, migration);
                }
            }
        }
    }
}