// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;

import java.util.Arrays;

/**
 * Stores all relevant information returned from {@link SchemaRegistryClient}/{@link SchemaRegistryAsyncClient} layer.
 */
public final class SchemaProperties {

    private final String schemaId;
    private final SerializationFormat serializationFormat;
    private final byte[] schemaBytes;
    private final String schemaName;

    /**
     * Initializes SchemaRegistryObject instance.
     *
     * @param schemaId the schema id
     * @param serializationFormat type of schema, e.g. avro, json
     * @param schemaName name of the schema.
     * @param schemaByteArray byte payload representing schema, returned from Azure Schema Registry
     */
    public SchemaProperties(
        String schemaId,
        SerializationFormat serializationFormat,
        String schemaName,
        byte[] schemaByteArray) {
        this.schemaId = schemaId;
        this.serializationFormat = serializationFormat;
        this.schemaBytes = schemaByteArray.clone();
        this.schemaName = schemaName;
    }

    /**
     * Returns the unique identifier for this schema.
     *
     * @return the unique identifier for this schema.
     */
    public String getSchemaId() {
        return schemaId;
    }

    /**
     * The serialization type of this schema.
     * @return schema type associated with the schema payload
     */
    public SerializationFormat getSerializationFormat() {
        return serializationFormat;
    }

    /**
     * The name of the schema.
     * @return the schema name.
     */
    public String getSchemaName() {
        return this.schemaName;
    }

    /**
     *  Schema bytes returned from Schema Registry.
     *
     *  @return The byte content of this schema.
     */
    public byte[] getSchema() {
        if (schemaBytes == null) {
            return new byte[0];
        }
        return Arrays.copyOf(this.schemaBytes, this.schemaBytes.length);
    }

}
