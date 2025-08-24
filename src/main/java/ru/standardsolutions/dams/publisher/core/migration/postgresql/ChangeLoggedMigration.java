package ru.standardsolutions.dams.publisher.core.migration.postgresql;

import ru.standardsolutions.dams.publisher.common.MigrationStep;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class ChangeLoggedMigration implements MigrationStep {

    private final DamsOptions options;
    private final MigrationStep action;

    public ChangeLoggedMigration(DamsOptions options, MigrationStep action) {
        this.options = options;
        this.action = action;
    }


    @Override
    public String id() {
        return "";
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        // Проверяем, выполнялась ли миграция
        String sqlCheckMigration = "SELECT 1 FROM " + options.changeLogTableName() + " WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sqlCheckMigration)) {
            ps.setString(1, action.id());
            if (ps.executeQuery().next()) {
                // logger.debug("Migration {} already applied, skipping", migrationId);
                return;
            }
        }

        action.execute(connection);

        // Записываем факт выполнения миграции
        String sqlInsertChangelog = "INSERT INTO " + options.changeLogTableName() + " (id, description, executed_date) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = connection.prepareStatement(sqlInsertChangelog)) {
            ps.setString(1, action.id());
            ps.setString(2, action.description());
            ps.executeUpdate();
            // logger.info("Migration '{}' recorded in changelog", migrationId);
        }
    }
}
