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
     * Date value.
     */
    private final List<FieldValue<?>> valueArray;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a ArrayValue.
     *
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueArray Array of field values.
     * @param pageNumber The page number on which this field exists.
     * @param elements The list of reference elements when includeTextDetails is set to true.
     */
    public ArrayValue(String text, BoundingBox boundingBox, List<FieldValue<?>> valueArray, int pageNumber,
        List<FormContent> elements) {
        super(text, boundingBox, pageNumber, elements);
        this.valueArray = valueArray;
        this.fieldValueType = FieldValueType.ARRAY;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FormContent> getElements() {
        return super.getElements();
    }
}
