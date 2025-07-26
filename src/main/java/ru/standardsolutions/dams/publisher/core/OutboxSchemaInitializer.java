package ru.standardsolutions.dams.publisher.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Класс для инициализации схемы outbox в базе данных PostgreSQL.
 * Выполняет SQL скрипт создания таблицы и индексов.
 */
public class OutboxSchemaInitializer {

    private static final String SCHEMA_SCRIPT_PATH = "/db/scripts/outbox_postgres_schema.sql";

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public OutboxSchemaInitializer(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Выполняет инициализацию схемы outbox
     */
    public void initializeSchema() throws SQLException, IOException {
        System.out.println("Начинаем инициализацию схемы outbox...");
        
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            String sqlScript = loadSchemaScript();
            executeSchemaScript(connection, sqlScript);
            System.out.println("Схема outbox успешно инициализирована");
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации схемы outbox: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Загружает SQL скрипт из ресурсов приложения
     */
    private String loadSchemaScript() throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(SCHEMA_SCRIPT_PATH)) {
            if (inputStream == null) {
                throw new IOException("Не удалось найти файл скрипта: " + SCHEMA_SCRIPT_PATH);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Выполняет SQL скрипт создания схемы
     */
    private void executeSchemaScript(Connection connection, String sqlScript) throws SQLException {
        String[] commands = sqlScript.split(";");
        
        try (Statement statement = connection.createStatement()) {
            for (String command : commands) {
                String trimmedCommand = command.trim();
                
                // Удаляем комментарии из команды
                String cleanCommand = removeComments(trimmedCommand);
                cleanCommand = cleanCommand.trim();
                
                if (!cleanCommand.isEmpty()) {
                    try {
                        statement.execute(cleanCommand);
                        System.out.println("Выполнена команда: " + 
                            cleanCommand.substring(0, Math.min(50, cleanCommand.length())) + "...");
                    } catch (SQLException e) {
                        System.err.println("Ошибка при выполнении команды: " + cleanCommand);
                        throw e;
                    }
                }
            }
        }
    }

    // Вспомогательный метод для удаления однострочных комментариев "--"
    private String removeComments(String sql) {
        StringBuilder result = new StringBuilder();
        String[] lines = sql.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("--")) {
                // Удаляем inline комментарии после кода
                int commentIdx = trimmed.indexOf("--");
                if (commentIdx >= 0) {
                    trimmed = trimmed.substring(0, commentIdx);
                }
                result.append(trimmed).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Пример использования класса
     */
    public static void main(String[] args) {
        // Пример конфигурации подключения к базе данных
        String jdbcUrl = "jdbc:postgresql://localhost:5432/your_database";
        String username = "your_username";
        String password = "your_password";

        OutboxSchemaInitializer initializer = new OutboxSchemaInitializer(jdbcUrl, username, password);
        
        try {
            initializer.initializeSchema();
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации: " + e.getMessage());
            e.printStackTrace();
        }
    }
}