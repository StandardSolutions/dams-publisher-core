package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import ru.standardsolutions.dams.publisher.core.data.DatabaseVoidOperation;

import java.sql.*;

record DoCreateChangeLogTable(Connection connection) implements DatabaseVoidOperation {


    @Override
    public void execute(String... args) {
        String schema = args[0];
        String tableName = args[1];
        String sql_exist_table = "SELECT 1 FROM pg_catalog.pg_class c " +
                "JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE n.nspname = ? AND c.relname = ? LIMIT 1";

        String sql_create_table = """
        CREATE TABLE IF NOT EXISTS %s (
            id SERIAL PRIMARY KEY,
            script_name TEXT NOT NULL,
            applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
        )
        """.formatted(tableName);
        try (PreparedStatement stmt = connection.prepareStatement(sql_exist_table)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            boolean isExist = stmt.executeQuery().next();
            if (isExist) return;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (
             Statement stmt = connection.createStatement()) {
            stmt.execute(sql_create_table);
            System.out.println("Таблица " + tableName + " создана (если её не было).");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании " + tableName, e);
        }
    }
}
