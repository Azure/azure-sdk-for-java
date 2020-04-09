// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

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
     *
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueNumber Floating point value.
     * @param pageNumber The 1 based page number of the document on which this field is found.
     */
    public FloatValue(String text, BoundingBox boundingBox, Float valueNumber, int pageNumber) {
        super(text, boundingBox, pageNumber);
        this.valueNumber = valueNumber;
        this.fieldValueType = FieldValueType.NUMBER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPageNumber() {
        return super.getPageNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BoundingBox getBoundingBox() {
        return super.getBoundingBox();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return super.getText();
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
