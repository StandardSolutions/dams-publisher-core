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

## Что важно для надёжности

Идемпотентность шага. В DDL используем IF NOT EXISTS / «создать, если нет» / «удалить, если есть». Для данных — upsert по ключу.

Один Connection + advisory‑lock. Лок на всю миграцию — исключаем параллельные прогоны.

Запись об ошибке (не обязательно). Можем логировать appendFailure(...) для диагностики, но наличие записи «успеха» — единственный сигнал «шаг сделан».

Версионирование шага (опционально). Если код шага поменялся, а ключ тот же — подумайте, нужно ли детектить «дрейф» (поле checksum/version).

## Памятка по идемпотентности шагов

PostgreSQL: CREATE TABLE IF NOT EXISTS, CREATE INDEX IF NOT EXISTS, ALTER TABLE ... ADD COLUMN IF NOT EXISTS, CREATE TYPE IF NOT EXISTS, jsonb тип.

H2: поддержка IF NOT EXISTS шире для базовых объектов, но нет CONCURRENTLY, поведение некоторых ALTER отличается — делайте отдельные шаги для H2.

Данные: INSERT ... ON CONFLICT (pk) DO NOTHING/UPDATE (PG), для H2 — MERGE INTO.

Побочные эффекты: избегайте операций, которые нельзя повторить без вреда (например, «перелить» данные без idempotent ключей).