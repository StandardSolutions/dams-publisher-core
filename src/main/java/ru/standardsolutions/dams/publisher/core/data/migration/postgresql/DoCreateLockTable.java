package ru.standardsolutions.dams.publisher.core.data.migration.postgresql;

import ru.standardsolutions.dams.publisher.core.data.Database;
import ru.standardsolutions.dams.publisher.core.data.DatabaseVoidOperation;

public record DoCreateLockTable(Database database) implements DatabaseVoidOperation {


    @Override
    public void execute(String ...args) {
        String schema = args[0];
        String tableName = args[1];
        String sql = "SELECT 1 FROM pg_catalog.pg_class c " +
                "JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "WHERE n.nspname = ? AND c.relname = ? LIMIT 1";
//        try (Connection connection = dataSource.getConnection();
//             PreparedStatement stmt = connection.prepareStatement(TABLE_EXISTS_SQL)) {
//            stmt.setString(1, schema);
//            stmt.setString(2, table);
//            try (ResultSet rs = stmt.executeQuery()) {
//                return rs.next();
//            }
//        }
    }
}
