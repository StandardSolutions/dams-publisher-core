package ru.standardsolutions.dams.publisher.core.migration.postgresql;

import ru.standardsolutions.dams.publisher.common.AdvisoryLock;
import ru.standardsolutions.dams.publisher.common.options.DataOptions;

import java.sql.*;
import java.util.Objects;

public class PgAdvisoryLock implements AdvisoryLock {

    private final Connection conn;
    private final DataOptions options;

    public PgAdvisoryLock(Connection conn, DataOptions options) {
        this.conn = Objects.requireNonNull(conn, "conn");
        this.options = Objects.requireNonNull(options, "opts");
    }

    @Override
    public void acquire() throws SQLException {
        long remaining = options.lockTimeoutMillis();

        try (PreparedStatement ps = conn.prepareStatement("SELECT pg_try_advisory_lock(?)")) {
            ps.setLong(1, options.lockId());

            while (remaining > 0) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getBoolean(1)) return;
                }
                try {
                    final long sleepStep = 1000L;
                    Thread.sleep(sleepStep);
                    remaining = remaining - sleepStep;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted while waiting for advisory lock", ex);
                }
            }
        }
        throw new SQLTimeoutException("pg_advisory_lock timeout for key=" + options.lockId());
    }

    @Override
    public void close() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT pg_advisory_unlock(?)")) {
            ps.setLong(1, options.lockId());
            ps.execute();
        }
    }
}
