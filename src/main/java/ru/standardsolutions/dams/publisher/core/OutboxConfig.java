package ru.standardsolutions.dams.publisher.core;

import java.util.List;

public class OutboxConfig {
    private final List<Recipient> recipients;

    public OutboxConfig(List<Recipient> recipients) {
        this.recipients = recipients;
    }

    public List<Recipient> getRecipients() { return recipients; }
} 