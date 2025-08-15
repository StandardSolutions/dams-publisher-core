package ru.standardsolutions.dams.publisher.core;

import javax.sql.DataSource;

public record DamsPublisherCore(DataSource dataSource) {

    public void execute() {
//       new DataMan(
//          new DmCreateLockTable(checkLockTable, createLockTable),
//          new DmLock(checkLockTable, createLockTable),
//          new DmExecute(script1, script2),
//          database

//       ).execute()
//        final DbScripts dbScripts = new DbScripts()
//        database.executeSQL(dbScripts);
//
//                try (Connection conn = dataSource.getConnection()) {
//            DatabaseMetaData metaData = conn.getMetaData();
//            metaData.getDatabaseProductName();
//        }
    }
}
