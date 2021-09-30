// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Locale;

/**
 * The list of all serialization types.
 */
public class SerializationFormat extends ExpandableStringEnum<SerializationFormat> {
    public static final SerializationFormat AVRO = fromString("avro");

    /**
     * Returns the {@link SerializationFormat} associated with the name.
     * @param name The name of the serialization type.
     * @return The {@link SerializationFormat} associated with this name.
     */
    public static SerializationFormat fromString(String name) {
        return fromString(name.toLowerCase(Locale.ROOT), SerializationFormat.class);
    }
}
