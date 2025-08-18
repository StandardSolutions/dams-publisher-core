package ru.standardsolutions.dams.publisher.core.data.options;

import ru.standardsolutions.dams.publisher.core.data.sanitized.SqlIdentifierSanitized;

import java.util.Optional;

public class DataOptions extends Options {

    public DataOptions(String... args) {
        super(args);
    }

    public DataOptions(Iterable<String> args) {
        super(args);
    }

    public String schema() {
        return map.getOrDefault("db-schema", "public");
    }

    public String tablePrefix() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-table-prefix", "dams_")
        ).value();
    }

    public int lockTimeout() {
        return Optional.ofNullable(map.get("lock-timeout"))
                .map(Integer::parseInt)
                .orElse(500);
    }

    public int lockId() {
        return this.lockTableName().hashCode();
    }

    public String changeLogTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-changelog-table-name", this.tablePrefix() + "db_changelog")
        ).value();
    }

    public String lockTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-lock-table-name", this.tablePrefix() + "db_lock")
        ).value();
    }

    public String recipientTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-recipient-table-name", this.tablePrefix() + "recipient")
        ).value();
    }

    public String outboxTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-outbox-table-name", this.tablePrefix() + "outbox")
        ).value();
    }

    public String messageTypeTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-message-type-table-name", this.tablePrefix() + "message_type")
        ).value();
    }

    public String recipientMessageTypeTableName() {
        return new SqlIdentifierSanitized(
                map.getOrDefault("db-recipient-message-type-table-name", this.tablePrefix() + "recipient_message_type")
        ).value();
    }
}