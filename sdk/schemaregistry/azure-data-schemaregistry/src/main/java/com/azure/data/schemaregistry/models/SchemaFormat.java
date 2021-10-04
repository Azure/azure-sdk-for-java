// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Locale;

/**
 * The list of all formats supported by Schema Registry.
 */
public final class SchemaFormat extends ExpandableStringEnum<SchemaFormat> {
    public static final SchemaFormat AVRO = fromString("avro");

    /**
     * Returns the {@link SchemaFormat} associated with the name.
     * @param name The name of the serialization type.
     * @return The {@link SchemaFormat} associated with this name.
     */
    public static SchemaFormat fromString(String name) {
        return fromString(name.toLowerCase(Locale.ROOT), SchemaFormat.class);
    }
}
