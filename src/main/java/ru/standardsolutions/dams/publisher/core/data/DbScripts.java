package ru.standardsolutions.dams.publisher.core.data;

import java.util.List;

public record DbScripts(DbType dbType) {
    public List<DbScript> scripts() {
        return List.of();
    }
}
