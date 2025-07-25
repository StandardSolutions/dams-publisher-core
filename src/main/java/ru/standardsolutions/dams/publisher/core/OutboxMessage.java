package ru.standardsolutions.dams.publisher.core;

import java.time.Instant;

public class OutboxMessage {
    private final String id;
    private final String payload;
    private final String type;
    private final String recipient;
    private final Instant createdAt;
    private Instant processedAt;

    public OutboxMessage(String id, String payload, String type, String recipient, Instant createdAt) {
        this.id = id;
        this.payload = payload;
        this.type = type;
        this.recipient = recipient;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getPayload() { return payload; }
    public String getType() { return type; }
    public String getRecipient() { return recipient; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
} 