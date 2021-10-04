// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.core.annotation.Immutable;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;

/**
 * Stores properties of a schema stored in Schema Registry.
 *
 * @see SchemaRegistryAsyncClient
 * @see SchemaRegistryClient
 */
@Immutable
public final class SchemaProperties {

    private final String schemaId;
    private final SchemaFormat schemaFormat;

    /**
     * Initializes a new instance.
     *
     * @param schemaId The schema id.
     * @param schemaFormat The type of schema, e.g. avro, json.
     */
    public SchemaProperties(String schemaId, SchemaFormat schemaFormat) {
        this.schemaId = schemaId;
        this.schemaFormat = schemaFormat;
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
     * The schema format of this schema.
     * @return schema type associated with the schema payload
     */
    public SchemaFormat getFormat() {
        return schemaFormat;
    }
}
