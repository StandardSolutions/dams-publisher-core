package ru.standardsolutions.dams.publisher.common;

/**
 * Base interface for all sanitizers
 */
public interface Sanitized<T> {
    T value() throws IllegalArgumentException;
}
