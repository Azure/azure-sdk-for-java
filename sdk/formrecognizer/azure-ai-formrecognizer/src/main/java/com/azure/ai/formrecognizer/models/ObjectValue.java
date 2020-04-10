// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.Map;

/**
 * Class to represent the Array value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueObject()}
 */
public class ObjectValue extends FieldValue<Map<String, FieldValue<?>>> {

    /*
     * Object value.
     */
    private final Map<String, FieldValue<?>> valueObject;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a ObjectValue.
     *
     * @param valueArray Array of field values.
     */
    public ObjectValue(Map<String, FieldValue<?>> valueArray) {
        this.valueObject = valueArray;
        this.fieldValueType = FieldValueType.OBJECT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, FieldValue<?>> getValue() {
        return this.valueObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }
}
