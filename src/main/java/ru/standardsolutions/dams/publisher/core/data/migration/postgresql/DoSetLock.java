package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import ru.standardsolutions.dams.publisher.core.data.DatabaseVoidOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

record DoSetLock(Connection connection) implements DatabaseVoidOperation {


    @Override
    public void execute(String... args) {
        try (Statement stmt = connection.createStatement()) {
            // Устанавливаем timeout для statement
            stmt.execute("SET statement_timeout = '500s'");
            
            // Получаем advisory lock
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT pg_advisory_lock(?)")) {
                ps.setInt(1, "dams-publisher-core".hashCode());
                ps.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}