// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * Class to represent the String value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueString()}
 */
public class StringValue extends FieldValue<String> {

    /*
     * String value.
     */
    private final String valueString;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a StringValue.
     *
     * @param valueString String value.
     */
    public StringValue(String valueString) {
        this.valueString = valueString;
        this.fieldValueType = FieldValueType.STRING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue() {
        return this.valueString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }

}
