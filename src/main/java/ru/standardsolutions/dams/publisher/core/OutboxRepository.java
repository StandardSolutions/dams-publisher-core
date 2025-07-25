package ru.standardsolutions.dams.publisher.core;

import java.util.List;

public interface OutboxRepository {
    void save(OutboxMessage message);
    List<OutboxMessage> fetchUnprocessed(String recipient, String type, int limit);
    void markProcessed(String messageId);
} 