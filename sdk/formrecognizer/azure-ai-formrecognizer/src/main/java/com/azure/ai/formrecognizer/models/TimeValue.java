// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * Class to represent the Time value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueTime()}
 */
public class TimeValue extends FieldValue<String> {

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /*
     * Time value.
     */
    private final String valueTime;

    /**
     * Constructs a Time Value.
     *
     * @param valueTime Time value.
     */
    public TimeValue(String valueTime) {
        this.valueTime = valueTime;
        this.fieldValueType = FieldValueType.STRING;
        // TODO: currently returning a string, waiting on swagger update.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue() {
        return this.valueTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }

}
