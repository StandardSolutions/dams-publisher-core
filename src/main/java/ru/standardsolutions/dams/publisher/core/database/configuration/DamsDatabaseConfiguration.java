package ru.standardsolutions.dams.publisher.core.database.configuration;

import ru.standardsolutions.dams.publisher.core.database.DatabaseType;

public class DamsDatabaseConfiguration {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final DatabaseType databaseType;

    private DamsDatabaseConfiguration(Builder builder) {
        this.jdbcUrl = builder.jdbcUrl;
        this.username = builder.username;
        this.password = builder.password;
        this.databaseType = builder.databaseType;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String jdbcUrl;
        private String username;
        private String password;
        private DatabaseType databaseType;

        private Builder() {
        }

        public Builder jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder databaseType(DatabaseType databaseType) {
            this.databaseType = databaseType;
            return this;
        }

        public DamsDatabaseConfiguration build() {
            return new DamsDatabaseConfiguration(this);
        }
    }
}