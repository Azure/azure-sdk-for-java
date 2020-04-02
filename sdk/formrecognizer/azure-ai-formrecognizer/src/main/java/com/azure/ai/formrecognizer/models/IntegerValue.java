// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

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
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueInteger Integer value.
     * @param pageNumber The 1 based page number of the document on which this field is found.
     */
    public IntegerValue(String text, BoundingBox boundingBox, Integer valueInteger, int pageNumber) {
        super(text, boundingBox, pageNumber);
        this.valueInteger = valueInteger;
        this.fieldValueType = FieldValueType.INTEGER;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Element> getElements() {
        return super.getElements();
    }

    @Override
    public Float getConfidence() {
        return super.getConfidence();
    }
}
