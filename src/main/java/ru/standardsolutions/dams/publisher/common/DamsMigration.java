package ru.standardsolutions.dams.publisher.common;

import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class DamsMigration {
    private final Database db;

    private final DamsOptions options;

    public DamsMigration(Database db, DamsOptions damsOptions) {
        this.db = db;
        this.options = damsOptions;
    }

    public void init(final List<MigrationStep> migrations) throws SQLException {
        try (Connection c = db.connection(); AdvisoryLock lock = db.advisoryLock(c, options)) {
            lock.acquire();

            ChangeLog changeLog = db.changelog();
            changeLog.init(c);

            for (MigrationStep migration : migrations) {
                if (changeLog.inserted(c, migration)) {
                    continue;
                }
                migration.execute(c);
                changeLog.insert(c, migration);
            }
        }
    }
}