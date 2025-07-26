# DAMS Publisher Core
Delivery-Assured Messages Publisher Core
Библиотека для работы с outbox паттерном в PostgreSQL.

## Использование в Spring Boot приложении

### 1. Добавьте зависимости в pom.xml



### 2. Настройте подключение к базе данных


### 3. Используйте один из вариантов инициализации

#### Вариант 1: Использование независимого класса

```java
@Component
public class OutboxSchemaInitializerSpring implements CommandLineRunner {
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Override
    public void run(String... args) throws Exception {
        OutboxSchemaInitializer initializer = new OutboxSchemaInitializer(jdbcUrl, username, password);
        initializer.initializeSchema();
    }
}
```

#### Вариант 2: Использование JdbcTemplate

```java
@Component
public class OutboxSchemaInitializerJdbcTemplate implements CommandLineRunner {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public OutboxSchemaInitializerJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Логика инициализации с JdbcTemplate
    }
}
```

### 4. Запустите приложение

```java
@SpringBootApplication
public class OutboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(OutboxApplication.class, args);
    }
}
```

## Использование без Spring Boot

```java
OutboxSchemaInitializer initializer = new OutboxSchemaInitializer(
    "jdbc:postgresql://localhost:5432/your_database",
    "your_username", 
    "your_password"
);

initializer.initializeSchema();
```

## SQL скрипт

Скрипт `src/main/resources/db/scripts/outbox_postgres_schema.sql` создает:
- Таблицу `outbox_message` для хранения сообщений
- Индексы для оптимизации запросов
