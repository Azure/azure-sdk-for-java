// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;
import java.time.LocalDate;

/**
 * Class to represent the Date value for
 * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueDate()}
 */
public class DateValue extends FieldValue<LocalDate> {

    /*
     * Date value.
     */
    private final LocalDate valueDate;

    /*
     * Type of the FieldValue.
     */
    private final FieldValueType fieldValueType;

    /**
     * Constructs a DateValue.
     *
     * @param text The text content of the extracted field.
     * @param boundingBox Bounding box of the field value.
     * @param valueDate Date value.
     * @param pageNumber The page number on which this field exists.
     */
    public DateValue(String text, BoundingBox boundingBox, LocalDate valueDate, int pageNumber) {
        super(text, boundingBox, pageNumber);
        this.valueDate = valueDate;
        this.fieldValueType = FieldValueType.DATE;
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
    public LocalDate getValue() {
        return this.valueDate;
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
