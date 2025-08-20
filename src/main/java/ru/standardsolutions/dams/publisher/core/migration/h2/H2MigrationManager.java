package ru.standardsolutions.dams.publisher.core.migration.h2;

import ru.standardsolutions.dams.publisher.common.MigrationLog;
import ru.standardsolutions.dams.publisher.common.MigrationManager;

import java.sql.Connection;
import java.sql.SQLException;

public final class H2MigrationManager implements MigrationManager {

    @Override
    public void execute(Connection connection, MigrationLog migrationLog) throws SQLException {

    }
}