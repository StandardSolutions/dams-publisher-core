package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import ru.standardsolutions.dams.publisher.core.data.DatabaseVoidOperation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

record DoReleaseLock(Connection connection) implements DatabaseVoidOperation {

    @Override
    public void execute(String... args) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SELECT pg_advisory_unlock(" + "dams-publisher-core".hashCode() + ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
