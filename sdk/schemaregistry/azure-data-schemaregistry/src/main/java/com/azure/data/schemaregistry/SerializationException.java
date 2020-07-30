// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.exception.AzureException;

/**
 * Exception thrown by Schema Registry serializer/deserializer implementations for runtime error cases.
 */
public class SerializationException extends AzureException {
    /**
     * @param s error message explaining serialization/deserialization operation failure
     */
    public SerializationException(String s) {
        super(s);
    }

    /**
     * @param s error message explaining serialization/deserialization operation failure
     * @param cause Throwable failure cause
     */
    public SerializationException(String s, Throwable cause) {
        super(s, cause);
    }
}
