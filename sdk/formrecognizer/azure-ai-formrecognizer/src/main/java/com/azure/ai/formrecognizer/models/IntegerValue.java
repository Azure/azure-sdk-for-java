// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * Class to represent the Integer value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueInteger()}
 */
public class IntegerValue extends FieldValue<Integer> {

    /*
     * Integer value.
     */
    private final Integer valueInteger;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs an IntegerValue.
     *
     * @param valueInteger Integer value.
     */
    public IntegerValue(Integer valueInteger) {
        this.valueInteger = valueInteger;
        this.fieldValueType = FieldValueType.INTEGER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getValue() {
        return this.valueInteger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }
}
