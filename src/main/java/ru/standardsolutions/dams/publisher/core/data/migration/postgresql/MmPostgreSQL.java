package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import ru.standardsolutions.dams.publisher.core.data.database.DataSourceException;
import ru.standardsolutions.dams.publisher.core.data.migration.MigrationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public record MmPostgreSQL(DataSource dataSource) implements MigrationManager {
    @Override
    public void execute() {
        try (Connection connection = dataSource.getConnection()) {
            new DoSetLock(connection).execute();
            new Do00CreateChangeLogTable(connection).execute("dams", "dams_changelog");
            new Do01CreateRecipientTable(connection).execute("dams", "dams_recipient");
            new Do02CreateOutboxTable(connection).execute("dams", "dams_outbox");
            new DoReleaseLock(connection).execute();
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }
}