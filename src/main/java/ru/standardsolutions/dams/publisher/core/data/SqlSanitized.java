package ru.standardsolutions.dams.publisher.core.data;

/**
 * Base interface for all sanitizers
 */
public interface SqlSanitized<T> {
    T value() throws IllegalArgumentException;
}
