// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

/**
 * Class to represent the String value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueString()}
 */
public class StringValue extends FieldValue<String> {

    /*
     * String value.
     */
    private String valueString;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a StringValue.
     *
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueString String value.
     * @param pageNumber The page number on which this field exists.
     */
    public StringValue(String text, BoundingBox boundingBox, String valueString, int pageNumber) {
        super(text, boundingBox, pageNumber);
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
    public void setValue(String value) {
        this.valueString = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldValueType getType() {
        return this.fieldValueType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Element> getElements() {
        return super.getElements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getConfidence() {
        return super.getConfidence();
    }
}
