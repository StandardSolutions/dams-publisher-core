package ru.standardsolutions.dams.publisher.common.migration.h2;

import ru.standardsolutions.dams.publisher.common.ChangeLog;
import ru.standardsolutions.dams.publisher.common.MigrationStep;

import java.sql.Connection;

public final class H2ChangeLog implements ChangeLog {
    @Override
    public void ensureExist(Connection c) {


    }

    @Override
    public boolean has(Connection c, MigrationStep migration) {
        return false;
    }

    @Override
    public void append(Connection c, MigrationStep migration) {

    }
}