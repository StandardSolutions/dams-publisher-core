package ru.standardsolutions.dams.publisher.core.data.database;

import ru.standardsolutions.dams.publisher.core.data.Database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public record JdbcDatabase(DataSource dataSource) implements Database {

    @Override
    public DbInfo info() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            return new DbInfo(
                    DbType.of(metaData.getDatabaseProductName()),
                    metaData.getDatabaseProductVersion()
            );
        } catch (SQLException e) {
            throw new DataSourceException(e);
        }
    }
}