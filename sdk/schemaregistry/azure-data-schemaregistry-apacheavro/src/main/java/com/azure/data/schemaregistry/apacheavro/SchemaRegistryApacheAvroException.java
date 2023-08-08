// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.exception.AzureException;

/**
 * Represents an exception that is thrown when Avro serialization or deserialization fails.
 */
public class SchemaRegistryApacheAvroException extends AzureException {
    /**
     * The id of the schema being processed when this exception occurred. {@code null} if there was none.
     */
    private final String schemaId;

    /**
     * Initializes a new instance.
     *
     * @param message The exception message.
     */
    public SchemaRegistryApacheAvroException(String message) {
        super(message);

        this.schemaId = null;
    }

    /**
     * Initializes a new instance.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     */
    public SchemaRegistryApacheAvroException(String message, Throwable cause) {
        super(message, cause);

        this.schemaId = null;
    }

    /**
     * Initializes a new instance.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     * @param enableSuppression Whether suppression is enabled or disabled.
     * @param writableStackTrace Whether the exception stack trace will be filled in.
     */
    public SchemaRegistryApacheAvroException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

        this.schemaId = null;
    }

    /**
     * Initializes a new instance.
     *
     * @param message The exception message.
     * @param cause The {@link Throwable} which caused the creation of this exception.
     * @param schemaId The id of the schema being processed when this exception occurred. {@code null} if there was
     *     none.
     */
    public SchemaRegistryApacheAvroException(String message, Throwable cause, String schemaId) {
        super(message, cause);
        this.schemaId = schemaId;
    }

    /**
     * Gets the schema id that was associated with this exception.
     *
     * @return The schema id associated with teh exception. {@code null} if there was no schema id.
     */
    public String getSchemaId() {
        return schemaId;
    }
}
