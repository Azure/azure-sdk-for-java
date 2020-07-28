// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.schemaregistry.client.SchemaRegistryClient;

/**
 * Common fields and helper methods for both the serializer and the deserializer.
 */
public abstract class AbstractDataSerDe {
    private final ClientLogger logger = new ClientLogger(AbstractDataSerDe.class);

    public static final int SCHEMA_ID_SIZE = 32;

    protected SchemaRegistryClient schemaRegistryClient;

    /**
     * Base constructor for all SerDe implementations.
     * @param schemaRegistryClient client to be used for sending or fetching schemas.
     * @throws IllegalArgumentException schemaRegistryClient parameter cannot be null
     */
    protected AbstractDataSerDe(SchemaRegistryClient schemaRegistryClient) {
        if (schemaRegistryClient == null) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Schema registry client must be initialized and passed into builder."));
        }
        this.schemaRegistryClient = schemaRegistryClient;
    }

    /**
     * Special case for Kafka serializer/deserializer implementations.
     */
    // special case for Kafka serializer/deserializer
    public AbstractDataSerDe() {

    }
}
