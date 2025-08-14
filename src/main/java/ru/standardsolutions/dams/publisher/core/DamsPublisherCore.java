package ru.standardsolutions.dams.publisher.core;

import ru.standardsolutions.dams.publisher.core.database.DbScripts;

import javax.sql.DataSource;

public record DamsPublisherCore(DataSource dataSource) {

    public void execute() {
        //final DbScripts dbScripts = new DbScripts()
        //database.executeSQL(dbScripts);

        //        try (Connection conn = dataSource.getConnection()) {
//            DatabaseMetaData metaData = conn.getMetaData();
//            metaData.getDatabaseProductName();
//        }
    }
}
