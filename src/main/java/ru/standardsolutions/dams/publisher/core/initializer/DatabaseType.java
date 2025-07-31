package ru.standardsolutions.dams.publisher.core.initializer;

public enum DatabaseType {

    POSTGRESQL("jdbc:postgresql"),

    H2("jdbc:h2");

    DatabaseType(String value) {
        this.value = value;
    }

    public final String value;

    /**
     * Detect database type of jdbc url.
     *
     * @param jdbcUrl jdbc url connection string.
     * @return DatabaseType enum.
     */
    public static DatabaseType ofJdbcUrl(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:postgresql")) return POSTGRESQL;
        if (jdbcUrl.startsWith("jdbc:h2")) return H2;
        throw new UnsupportedDatabaseException("Supported Database Type: PostgreSQL, H2. jdbcUrl: " + jdbcUrl);
    }
}