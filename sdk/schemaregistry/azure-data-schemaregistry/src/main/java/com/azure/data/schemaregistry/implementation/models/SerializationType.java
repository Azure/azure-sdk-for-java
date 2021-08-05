// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for SerializationType. */
public final class SerializationType extends ExpandableStringEnum<SerializationType> {
    /** Static value avro for SerializationType. */
    public static final SerializationType AVRO = fromString("avro");

    /**
     * Creates or finds a SerializationType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SerializationType.
     */
    @JsonCreator
    public static SerializationType fromString(String name) {
        return fromString(name, SerializationType.class);
    }

    /** @return known SerializationType values. */
    public static Collection<SerializationType> values() {
        return values(SerializationType.class);
    }
}
