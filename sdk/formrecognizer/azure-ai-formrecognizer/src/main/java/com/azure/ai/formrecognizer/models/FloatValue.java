// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * Class to represent the Float value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueNumber()}
 */
public class FloatValue extends FieldValue<Float> {

    /*
     * Floating point value.
     */
    private final Float valueNumber;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a FloatValue.

     * @param valueNumber Floating point value.

     */
    public FloatValue(Float valueNumber) {
        this.valueNumber = valueNumber;
        this.fieldValueType = FieldValueType.NUMBER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getValue() {
        return this.valueNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }
}
