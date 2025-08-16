package ru.standardsolutions.dams.publisher.core;

import ru.standardsolutions.dams.publisher.core.data.migration.MigrationManager;
import ru.standardsolutions.dams.publisher.core.data.database.DbInfo;
import ru.standardsolutions.dams.publisher.core.data.database.DbType;
import ru.standardsolutions.dams.publisher.core.data.database.JdbcDatabase;
import ru.standardsolutions.dams.publisher.core.data.h2.DmH2;
import ru.standardsolutions.dams.publisher.core.data.migration.postgresql.MmPostgreSQL;

import javax.sql.DataSource;
import java.util.Map;

public record DamsPublisherCore(DataSource dataSource) {

    public void execute() {
        JdbcDatabase db = new JdbcDatabase(dataSource);
        DbInfo dbInfo = db.info();
        Map<DbType, MigrationManager> managerMap = Map.of(
                DbType.POSTGRESQL, new MmPostgreSQL(),
                DbType.H2, new DmH2()
        );
        MigrationManager migrationManager = managerMap.get(dbInfo.type());
        migrationManager.execute();
    }
}
