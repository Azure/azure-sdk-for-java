// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import java.util.List;

/**
 * Represents teh value of {@link FieldValue}.
 *
 * @param <T> Type of {@link FieldValue fieldValue}.
 */
public abstract class FieldValue<T> extends RawItem {

    /*
     * The 1-based page number in the input document.
     */
    private final int pageNumber;

    /**
     * List of references to the text elements constituting this field.
     */
    private final List<FormContent> elements;

    /**
     * Constructs a {@code FieldValue fieldValue} to describe fields on
     * {@link com.azure.ai.formrecognizer.implementation.models.DocumentResult}
     *  @param text Text content of the extracted field.
     * @param boundingBox Bounding Box of the extracted field.
     * @param pageNumber The page number of teh extracted receipt on which this field exists
     * @param elements The set of reference elements when includeTextDetails is set to true.
     */
    FieldValue(String text, BoundingBox boundingBox, int pageNumber, final List<FormContent> elements) {
        super(text, boundingBox);
        this.pageNumber = pageNumber;
        this.elements = elements;
    }

    /**
     * Gets the field value.
     *
     * @return The T field value
     */
    public abstract T getValue();

    /**
     * Get the 1-based page number in the input document.
     *
     * @return the page number value.
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * Gets the {@link FieldValueType type} the field value.
     *
     * @return The {@link FieldValueType type} the field value.
     */
    public abstract FieldValueType getType();

    /**
     * Get the elements property: When includeTextDetails is set to true, a
     * list of references to the text elements constituting this field.
     *
     * @return the elements value.
     */
    public List<FormContent> getElements() {
        return this.elements;
    }
}
