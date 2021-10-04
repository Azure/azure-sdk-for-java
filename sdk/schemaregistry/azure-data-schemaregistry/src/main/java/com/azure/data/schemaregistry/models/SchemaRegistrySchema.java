// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.core.annotation.Immutable;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;

/**
 * Represents a schema in Schema Registry.
 *
 * @see SchemaRegistryAsyncClient
 * @see SchemaRegistryClient
 */
@Immutable
public final class SchemaRegistrySchema {
    private final SchemaProperties properties;
    private final String content;

    /**
     * Creates a new instance.
     *
     * @param properties Schema's properties.
     * @param schemaDefinition The definition of the schema.
     */
    public SchemaRegistrySchema(SchemaProperties properties, String schemaDefinition) {
        this.properties = properties;
        this.content = schemaDefinition;
    }

    /**
     * Gets properties related to the schema.
     *
     * @return Properties of the schema.
     */
    public SchemaProperties getProperties() {
        return properties;
    }

    /**
     * Gets the content of the schema.
     *
     * @return The content of the schema.
     */
    public String getSchemaDefinition() {
        return content;
    }
}
