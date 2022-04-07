// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.schemaregistry.apacheavro;

import com.azure.core.experimental.models.MessageWithMetadata;

import java.time.OffsetDateTime;

/**
 * Sample type used in {@link SchemaRegistryApacheAvroSerializerJavaDocCodeSamples#serializeMessageFactory()}.
 */
public class ComplexMessage extends MessageWithMetadata {
    private final String id;
    private final OffsetDateTime creationTime;

    /**
     * Creates a message with the id.
     *
     * @param id The id to use.
     * @param creationTime Time message was created.
     */
    public ComplexMessage(String id, OffsetDateTime creationTime) {
        this.id = id;
        this.creationTime = creationTime;
    }

    /**
     * Gets the id.
     *
     * @return The id.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the creation time.
     *
     * @return The creation time.
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }
}
