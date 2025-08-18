package ru.standardsolutions.dams.publisher.core.migration.postgresql;

import ru.standardsolutions.dams.publisher.common.MigrationAction;
import ru.standardsolutions.dams.publisher.common.options.DataOptions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

final class AdvisoryLockedAction implements MigrationAction {

    private final DataOptions options;

    private final List<MigrationAction> actions;

    public AdvisoryLockedAction(DataOptions options, MigrationAction... actions) {
        this.options = options;
        this.actions = List.of(actions);
    }

    @Override
    public String id() {
        return "LockedMigrations";
    }

    @Override
    public String description() {
        return "Migrations executed under lock";
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        acquireLock(connection);
        SQLException originalException = null;
        try {
            for (MigrationAction action : actions) {
                action.execute(connection);
            }
        } catch (SQLException e) {
            originalException = e;
            throw e;
        } finally {
            try {
                releaseLock(connection);
            } catch (SQLException unlockEx) {
                if (originalException != null) {
                    originalException.addSuppressed(unlockEx);
                } else {
                    throw unlockEx;
                }
            }
        }
    }

    private void acquireLock(Connection connection) throws SQLException {
        try (var stmt = connection.prepareStatement("SELECT pg_advisory_lock(?)")) {
            stmt.setInt(1, options.lockId());
            stmt.execute();
        }
    }

    private void releaseLock(Connection connection) throws SQLException {
        try (var stmt = connection.prepareStatement("SELECT pg_advisory_unlock(?)")) {
            stmt.setInt(1, options.lockId());
            stmt.execute();
        }
    }
}
