// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

/**
 * Class to represent the Array value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueArray()}
 */
public class ArrayValue extends FieldValue<List<FieldValue<?>>> {

    /*
     * List of field values.
     */
    private final List<FieldValue<?>> valueArray;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a ArrayValue.
     *
     * @param valueArray Array of field values.
     */
    public ArrayValue(List<FieldValue<?>> valueArray) {
        this.valueArray = valueArray;
        this.fieldValueType = FieldValueType.ARRAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FieldValue<?>> getValue() {
        return this.valueArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }
}
