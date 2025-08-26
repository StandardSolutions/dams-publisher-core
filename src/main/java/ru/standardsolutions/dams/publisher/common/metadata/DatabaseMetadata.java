package ru.standardsolutions.dams.publisher.common.metadata;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseMetadata {

    private final DatabaseType type;

    private final String version;

    public DatabaseMetadata(Connection conn) throws SQLException {
        java.sql.DatabaseMetaData metaData = conn.getMetaData();
        this.type = DatabaseType.of(metaData.getDatabaseProductName());
        this.version = metaData.getDatabaseProductVersion();
    }

    public DatabaseType type() {
        return type;
    }

    public String version() {
        return version;
    }
}
