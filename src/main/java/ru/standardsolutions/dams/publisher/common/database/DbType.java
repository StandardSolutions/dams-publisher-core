package ru.standardsolutions.dams.publisher.common.database;

public enum DbType {

    POSTGRESQL("postgreSQL"),

    H2("H2");

    DbType(String value) {
        this.value = value;
    }

    public final String value;

    /**
     * Detect database type of jdbc url.
     *
     * @param jdbcUrl jdbc url connection string.
     * @return DatabaseType enum.
     */
    public static DbType ofJdbcUrl(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:postgresql://")) return POSTGRESQL;
        if (jdbcUrl.startsWith("jdbc:h2://")) return H2;
        throw new UnsupportedDatabaseException("Supported Database Type: PostgreSQL, H2. jdbcUrl: " + jdbcUrl);
    }

    /**
     * Database type from string.
     *
     * @param productName as string.
     * @return DatabaseType enum.
     */
    public static DbType of(String productName) {
        for (DbType type : values()) {
            if (type.name().equalsIgnoreCase(productName)) {
                return type;
            }
        }
        throw new UnsupportedDatabaseException("Supported Database Type: PostgreSQL, H2. Current database: " + productName);
    }
}