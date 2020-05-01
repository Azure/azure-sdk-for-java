/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

/**
 * Exception thrown by Schema Registry client deserializer implementations for runtime error cases.
 */
public class SerializationException extends Exception {
    public SerializationException(String s) {
        super(s);
    }

    public SerializationException(String s, Throwable cause) {
        super(s, cause);
    }
}
