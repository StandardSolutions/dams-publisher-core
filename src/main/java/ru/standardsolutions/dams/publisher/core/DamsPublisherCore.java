package ru.standardsolutions.dams.publisher.core;

import ru.standardsolutions.dams.publisher.core.data.database.DbInfo;
import ru.standardsolutions.dams.publisher.core.data.database.DbType;
import ru.standardsolutions.dams.publisher.core.data.database.JdbcDatabase;
import ru.standardsolutions.dams.publisher.core.data.migration.h2.DmH2;
import ru.standardsolutions.dams.publisher.core.data.migration.postgresql.MmPostgreSQL;

import javax.sql.DataSource;

public record DamsPublisherCore(DataSource dataSource, String ...args) {

    public void execute() {
        JdbcDatabase db = new JdbcDatabase(dataSource);
        DbInfo dbInfo = db.info();
        
        if (dbInfo.type() == DbType.H2) {
            DmH2 h2Manager = new DmH2();
            h2Manager.execute(dataSource);
        } else if (dbInfo.type() == DbType.POSTGRESQL) {
            MmPostgreSQL postgresManager = new MmPostgreSQL(dataSource);
            postgresManager.execute();
        } else {
            throw new UnsupportedOperationException("Unsupported database type: " + dbInfo.type());
        }
    }
}
