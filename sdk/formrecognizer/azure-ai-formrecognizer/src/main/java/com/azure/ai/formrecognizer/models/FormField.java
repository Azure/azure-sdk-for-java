// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The FormField model.
 *
 * @param <T> The type of FormField.
 */
@Immutable
public final class FormField<T> {

    /*
     * The confidence value of the field.
     */
    private final float confidence;

    /*
     * The label text of the field.
     */
    private final FieldText labelText;

    /*
     * The name value of the field.
     */
    private final String name;

    /*
     * The value of the field.
     */
    private final T fieldValue;

    /*
     * The text value field..
     */
    private final FieldText valueText;

    /*
     * The 1 based page number.
     */
    private final Integer pageNumber;

    /**
     * Constructs a FormField object.
     *
     * @param confidence The confidence of the recognized field.
     * @param labelText The label text value for the field.
     * @param name The name the field.
     * @param fieldValue The value of the field.
     * @param valueText The label value text for the field.
     * @param pageNumber The label text value for the field.
     */
    public FormField(final float confidence, final FieldText labelText, final String name, final T fieldValue,
        final FieldText valueText, final Integer pageNumber) {
        this.confidence = confidence;
        this.labelText = labelText;
        this.name = name;
        this.fieldValue = fieldValue;
        this.valueText = valueText;
        this.pageNumber = pageNumber;
    }

    /**
     * Get the estimated confidence value of the recognized field.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Get the label text of the field.
     *
     * @return the text-label value.
     */
    public FieldText getLabelText() {
        return this.labelText;
    }

    /**
     * Get the name of the field in the provided document.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the value of the field.
     *
     * @return Value of the field.
     */
    public T getFieldValue() {
        return this.fieldValue;
    }

    /**
     * Get the value text of the field.
     *
     * @return the value text of the field.
     */
    public FieldText getValueText() {
        return this.valueText;
    }

    /**
     * Get the 1-based page number in the input document.
     *
     * @return the page number value.
     */
    public Integer getPageNumber() {
        return this.pageNumber;
    }
}
