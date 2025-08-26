package ru.standardsolutions.dams.publisher.common.migration.h2;

import ru.standardsolutions.dams.publisher.common.AdvisoryLock;
import ru.standardsolutions.dams.publisher.common.ChangeLog;
import ru.standardsolutions.dams.publisher.common.Database;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import javax.sql.DataSource;
import java.sql.Connection;

public final class H2Database implements Database {

    private final AdvisoryLock advisoryLock;

    private final ChangeLog changeLog;

    public H2Database(final AdvisoryLock advisoryLock, final ChangeLog changeLog) {
        this.advisoryLock = advisoryLock;
        this.changeLog = changeLog;
    }

    @Override
    public AdvisoryLock newLock(Connection c, DamsOptions options) {
        return null;
    }

    @Override
    public ChangeLog changelog() {
        return new H2ChangeLog();
    }
}