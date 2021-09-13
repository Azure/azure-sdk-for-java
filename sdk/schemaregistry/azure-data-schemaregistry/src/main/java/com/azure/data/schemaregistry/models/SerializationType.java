// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Locale;

/**
 * The list of all serialization types.
 */
public class SerializationType extends ExpandableStringEnum<SerializationType> {
    public static final SerializationType AVRO = fromString("avro");

    /**
     * Returns the {@link SerializationType} associated with the name.
     * @param name The name of the serialization type.
     * @return The {@link SerializationType} associated with this name.
     */
    public static SerializationType fromString(String name) {
        return fromString(name.toLowerCase(Locale.ROOT), SerializationType.class);
    }
}
