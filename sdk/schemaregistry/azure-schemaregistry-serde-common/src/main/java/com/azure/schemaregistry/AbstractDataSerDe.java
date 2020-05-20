/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import com.azure.schemaregistry.client.SchemaRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common fields and helper methods for both the serializer and the deserializer.
 */
public abstract class AbstractDataSerDe {
    public static final int idSize = 64;

    protected SchemaRegistryClient schemaRegistryClient;

    protected AbstractDataSerDe(SchemaRegistryClient schemaRegistryClient) {
        if (schemaRegistryClient == null) {
            throw new IllegalArgumentException("Schema registry client must be initialized and passed into builder.");
        }
        this.schemaRegistryClient = schemaRegistryClient;
    }

    // special case for Kafka serializer/deserializer
    public AbstractDataSerDe() {

    }
}
