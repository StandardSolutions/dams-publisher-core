package ru.standardsolutions.dams.publisher.common;

import ru.standardsolutions.dams.publisher.common.database.JdbcDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public interface MigrationManager {
    void execute(Connection connection, MigrationLog migrationLog) throws SQLException;
}
