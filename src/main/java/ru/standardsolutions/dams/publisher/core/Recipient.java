package ru.standardsolutions.dams.publisher.core;

import java.util.Set;

public class Recipient {
    private final String name;
    private final Set<String> supportedTypes;

    public Recipient(String name, Set<String> supportedTypes) {
        this.name = name;
        this.supportedTypes = supportedTypes;
    }

    public String getName() { return name; }
    public Set<String> getSupportedTypes() { return supportedTypes; }
} 