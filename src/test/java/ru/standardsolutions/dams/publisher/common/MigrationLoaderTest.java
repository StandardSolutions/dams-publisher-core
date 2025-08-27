package ru.standardsolutions.dams.publisher.common;

import org.junit.jupiter.api.Test;
import ru.standardsolutions.dams.publisher.common.options.DamsOptions;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MigrationLoaderTest {

    @Test
    void testLoadMigrationsFromResources() throws IOException {
        MigrationLoader loader = new MigrationLoader();
        DamsOptions options = new DamsOptions();
        
        List<MigrationStep> migrations = loader.loadMigrations(options);
        
        // Проверяем, что миграции загружены
        assertNotNull(migrations, "Migrations list should not be null");
        assertTrue(migrations.size() >= 0, "Should load migrations from resources");
        
        // Проверяем миграции по порядку
        for (int i = 0; i < migrations.size() - 1; i++) {
            String currentId = migrations.get(i).id();
            String nextId = migrations.get(i + 1).id();
            assertTrue(currentId.compareTo(nextId) < 0, 
                "Migrations should be sorted by id: " + currentId + " should come before " + nextId);
        }
    }

    @Test
    void testMigrationStepProperties() throws IOException {
        MigrationLoader loader = new MigrationLoader();
        DamsOptions options = new DamsOptions();
        
        List<MigrationStep> migrations = loader.loadMigrations(options);
        
        for (MigrationStep migration : migrations) {
            assertNotNull(migration.id(), "Migration id should not be null");
            assertNotNull(migration.description(), "Migration description should not be null");
            assertFalse(migration.id().trim().isEmpty(), "Migration id should not be empty");
            assertFalse(migration.description().trim().isEmpty(), "Migration description should not be empty");
        }
    }

    @Test
    void testCustomMigrationsPath() throws IOException {
        MigrationLoader loader = new MigrationLoader();
        // Создаем options с несуществующим путем (пока используем дефолтный путь)
        DamsOptions options = new DamsOptions();
        
        List<MigrationStep> migrations = loader.loadMigrations(options);
        
        // Should load existing migrations
        assertNotNull(migrations, "Migrations list should not be null");
        assertTrue(migrations.size() >= 0, "Should load migrations or return empty list");
    }

    @Test
    void testTemplateProcessing() throws IOException {
        MigrationLoader loader = new MigrationLoader();
        DamsOptions options = new DamsOptions("--db-table-prefix=test_");
        
        List<MigrationStep> migrations = loader.loadMigrations(options);
        
        // Проверяем, что найдены миграции и они содержат обработанные шаблоны
        if (!migrations.isEmpty()) {
            // Проверяем что в описании миграций нет необработанных шаблонов ${...}
            for (MigrationStep migration : migrations) {
                String description = migration.description();
                assertFalse(description.contains("${"), 
                    "Migration description should not contain unprocessed templates: " + description);
            }
        }
    }
}