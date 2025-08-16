package ru.standardsolutions.dams.publisher.core.data;

public interface DatabaseOperation<R> {

    R execute(String ...args);
}