package com.microsoft.windowsazure.services.core.storage;

/**
 * 
 * Specifies which types of operations the service should log.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
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
