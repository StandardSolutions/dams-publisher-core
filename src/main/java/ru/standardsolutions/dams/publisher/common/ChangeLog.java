package ru.standardsolutions.dams.publisher.common;

import java.sql.Connection;

public interface ChangeLog {
    void init(Connection c);

    boolean inserted(Connection c, MigrationStep migration);

    void insert(Connection c, MigrationStep migration);
}
