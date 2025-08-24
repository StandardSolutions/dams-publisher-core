package ru.standardsolutions.dams.publisher.common;

import java.sql.Connection;
import java.sql.SQLException;

public interface MigrationStep {
    String id();
    String description();
    void execute(Connection connection) throws SQLException;
}