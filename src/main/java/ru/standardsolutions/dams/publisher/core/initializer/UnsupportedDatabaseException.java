package ru.standardsolutions.dams.publisher.core.initializer;

/**
 * Исключение, выбрасываемое когда тип базы данных не поддерживается
 * или не может быть определен по JDBC URL.
 */
public class UnsupportedDatabaseException extends RuntimeException {
    
    public UnsupportedDatabaseException(String message) {
        super(message);
    }
    
    public UnsupportedDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UnsupportedDatabaseException(Throwable cause) {
        super(cause);
    }
} 