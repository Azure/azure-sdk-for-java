// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

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
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueTime Time value.
     * @param pageNumber The page number on which this field exists.
     */
    public TimeValue(String text, BoundingBox boundingBox, String valueTime, int pageNumber) {
        super(text, boundingBox, pageNumber);
        this.valueTime = valueTime;
        this.fieldValueType = FieldValueType.STRING;
        // TODO: currently returning a string, waiting on swagger update.
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
