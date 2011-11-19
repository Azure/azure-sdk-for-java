package com.microsoft.windowsazure.services.core.storage;

/**
 * Specifies the type of a continuation token.
 */
public enum ResultContinuationType {
    /**
     * Specifies no continuation.
     */
    NONE,

    /**
     * Specifies the token is a blob listing continuation token.
     */
    BLOB,

    /**
     * Specifies the token is a container listing continuation token.
     */
    CONTAINER,

    /**
     * Specifies the token is a queue listing continuation token (reserved for future use).
     */
    QUEUE,

    /**
     * Specifies the token is a table query continuation token (reserved for future use).
     */
    TABLE
}
