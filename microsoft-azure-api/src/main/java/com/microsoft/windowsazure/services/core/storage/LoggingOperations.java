package com.microsoft.windowsazure.services.core.storage;

/**
 * 
 * Specifies which types of operations the service should log.
 */
public enum LoggingOperations {
    /**
     * Log Read Operations .
     */
    READ,

    /**
     * Log Write Operations.
     */
    WRITE,

    /**
     * Log Delete Operations.
     */
    DELETE;
}
