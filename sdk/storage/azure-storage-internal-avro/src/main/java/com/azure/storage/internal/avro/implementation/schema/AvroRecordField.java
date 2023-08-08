// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema;

/**
 * Represents a Field in a Record.
 */
public class AvroRecordField {

    private final String name;
    private final AvroType type;

    /**
     * Creates a new AvroRecordField.
     *
     * @param name The name of the field.
     * @param type The type of the field.
     */
    AvroRecordField(String name, AvroType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type.
     */
    public AvroType getType() {
        return type;
    }
}
