package ru.standardsolutions.dams.publisher.core.data;

/**
 * Base interface for all sanitizers
 */
public interface Sanitized<T> {
    T value() throws IllegalArgumentException;
}
