package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class LockManager {
    private final Connection connection;
    private static final int LOCK_ID = "dams-publisher-core".hashCode();

    public LockManager(Connection connection) {
        this.connection = connection;
    }

    public void withLock(Runnable action) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET statement_timeout = '500s'");
            stmt.execute("SELECT pg_advisory_lock(" + LOCK_ID + ")");

            action.run();

        } finally {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SELECT pg_advisory_unlock(" + LOCK_ID + ")");
            }
        }
    }
}
