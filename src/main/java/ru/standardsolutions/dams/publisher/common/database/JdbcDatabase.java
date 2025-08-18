package ru.standardsolutions.dams.publisher.common.database;

import ru.standardsolutions.dams.publisher.common.Database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public final class JdbcDatabase implements Database {

    private final DbInfo dbInfo;

    public JdbcDatabase(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            this.dbInfo = new DbInfo(
                    DbType.of(metaData.getDatabaseProductName()),
                    metaData.getDatabaseProductVersion()
            );
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
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