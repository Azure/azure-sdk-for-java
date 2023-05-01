// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschemavalidator;

import com.azure.core.annotation.Immutable;

/**
 * Package-private class that holds additional options when creating serializer.
 */
@Immutable
class SerializerOptions {
    private final int maxCacheSize;
    private final String schemaGroup;

    /**
     * Creates a new instance.
     *
     * @param schemaGroup Optional schema group when registering a schema is required.
     * @param maxCacheSize The maximum cache size for the serializer.
     */
    SerializerOptions(String schemaGroup, int maxCacheSize) {
        this.schemaGroup = schemaGroup;
        this.maxCacheSize = maxCacheSize;
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
}
