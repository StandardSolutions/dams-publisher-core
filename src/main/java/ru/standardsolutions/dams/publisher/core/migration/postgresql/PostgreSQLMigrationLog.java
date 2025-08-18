package ru.standardsolutions.dams.publisher.core.migration.postgresql;

import ru.standardsolutions.dams.publisher.common.MigrationAction;
import ru.standardsolutions.dams.publisher.common.MigrationLog;
import ru.standardsolutions.dams.publisher.common.options.DataOptions;

import java.util.List;

public final class PostgreSQLMigrationLog implements MigrationLog {

    private final List<MigrationAction> migrationLog;

    public PostgreSQLMigrationLog(DataOptions dataOptions) {
        this.migrationLog = List.of(
                new Ma00CreateChangeLogTable(dataOptions),
                new AdvisoryLockedAction(
                        dataOptions,
                        new ChangeLoggedMigration(dataOptions, new Ma01CreateRecipient(dataOptions)),
                        new ChangeLoggedMigration(dataOptions, new Ma02CreateOutboxTable(dataOptions))
                )
        );
    }

    @Override
    public List<MigrationAction> actions() {
        return migrationLog;
    }
}