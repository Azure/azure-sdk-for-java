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

    private final String id;
    private final SchemaFormat format;

    /**
     * Initializes a new instance.
     *
     * @param id The schema id.
     * @param format The type of schema, e.g. avro, json.
     */
    public SchemaProperties(String id, SchemaFormat format) {
        this.id = id;
        this.format = format;
    }

    /**
     * Returns the unique identifier for this schema.
     *
     * @return the unique identifier for this schema.
     */
    public String getId() {
        return id;
    }

    /**
     * The schema format of this schema.
     * @return schema type associated with the schema payload
     */
    public SchemaFormat getFormat() {
        return format;
    }
}
