package ru.standardsolutions.dams.publisher.common.migration.postgresql;

import ru.standardsolutions.dams.publisher.common.AdvisoryLock;
import ru.standardsolutions.dams.publisher.common.Database;
import ru.standardsolutions.dams.publisher.common.database.DataSourceException;
import ru.standardsolutions.dams.publisher.common.database.DbInfo;
import ru.standardsolutions.dams.publisher.common.database.DbType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public final class PgDatabase implements Database {

    private final DbInfo dbInfo;

    private final AdvisoryLock advisoryLock;

    public PgDatabase(DataSource dataSource, AdvisoryLock advisoryLock) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            this.dbInfo = new DbInfo(
                    DbType.of(metaData.getDatabaseProductName()),
                    metaData.getDatabaseProductVersion()
            );
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
        this.advisoryLock = advisoryLock;
    }

    @Override
    public DbType type() {
        return dbInfo.type();
    }

    @Override
    public String version() {
        return dbInfo.version();
    }
}