// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.core.annotation.Immutable;
import com.azure.data.schemaregistry.SchemaRegistryAsyncClient;
import com.azure.data.schemaregistry.SchemaRegistryClient;
import com.azure.data.schemaregistry.implementation.SchemaRegistryHelper;

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
    private final String groupName;
    private final String name;

    static {
        SchemaRegistryHelper.setAccessor(new SchemaRegistryHelper.SchemaRegistryModelsAccessor() {
            @Override
            public SchemaProperties getSchemaProperties(String id, SchemaFormat format, String groupName, String name) {
                return new SchemaProperties(id, format, groupName, name);
            }
        });
    }

    /**
     * Initializes a new instance.
     *
     * @param id The schema id.
     * @param format The type of schema, e.g. avro, json.
     */
    public SchemaProperties(String id, SchemaFormat format) {
        this(id, format, null, null);
    }

    /**
     * Initializes a new instance.
     *
     * @param id The schema id.
     * @param format The type of schema, e.g. avro, json.
     * @param groupName The schema group for this schema.
     * @param name The name of the schema.
     */
    SchemaProperties(String id, SchemaFormat format, String groupName, String name) {
        this.id = id;
        this.format = format;
        this.groupName = groupName;
        this.name = name;
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

    /**
     * Gets the schema group of this schema.
     *
     * @return The schema group of this schema.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Gets the name of the schema.
     *
     * @return The name of the schema.
     */
    public String getName() {
        return name;
    }
}
