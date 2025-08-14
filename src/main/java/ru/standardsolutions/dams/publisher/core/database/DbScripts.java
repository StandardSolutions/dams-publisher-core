package ru.standardsolutions.dams.publisher.core.database;

import java.util.List;

public record DbScripts(DbType dbType) {
    public List<DbScript> scripts() {
        return List.of();
    }
}
