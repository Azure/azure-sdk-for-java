// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.serializer.JsonSerializer;

import java.util.Objects;

/**
 * Package-private class that holds additional options when creating serializer.
 */
@Immutable
class SerializerOptions {
    private final boolean autoRegisterSchemas;
    private final int maxCacheSize;
    private final String schemaGroup;
    private final JsonSerializer jsonSerializer;

    /**
     * Creates a new instance.
     *
     * @param schemaGroup Optional schema group when registering a schema is required.
     * @param autoRegisterSchemas {@code true} to register schema if it does not exist, {@code false} otherwise.
     * @param maxCacheSize The maximum cache size for the serializer.
     */
    SerializerOptions(String schemaGroup, boolean autoRegisterSchemas, int maxCacheSize,
        JsonSerializer jsonSerializer) {
        this.schemaGroup = schemaGroup;
        this.autoRegisterSchemas = autoRegisterSchemas;
        this.maxCacheSize = maxCacheSize;

        this.jsonSerializer = Objects.requireNonNull(jsonSerializer, "'jsonSerializer' is required.");
    }

    /**
     * Gets whether to auto-register schemas.
     *
     * @return {@code true} to register schema if it does not exist; {@code false} otherwise.
     */
    public boolean autoRegisterSchemas() {
        return autoRegisterSchemas;
    }

    /**
     * Gets the maximum cache size.
     *
     * @return The maximum cache size.
     */
    int getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * Gets the schema group to register schemas against.
     *
     * @return The schema group.
     */
    String getSchemaGroup() {
        return schemaGroup;
    }

    /**
     * Gets the serializer used for serialization.
     *
     * @return The serializer.
     */
    public JsonSerializer getJsonSerializer() {
        return jsonSerializer;
    }
}
