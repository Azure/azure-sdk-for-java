// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.Collections;
import java.util.List;

/**
 * The FieldData model.
 */
@Immutable
public final class FieldData extends FormElement {

    /**
     * The list of element references for the field value.
     */
    private final List<FormElement> fieldElements;

    /**
     * Creates raw OCR FieldData item.
     *
     * @param text The text content of ExtractedField.
     * @param boundingBox The Bounding Box of the recognized field.
     * @param pageNumber the 1 based page number.
     * @param fieldElements The list of element references when includeFieldElements is set to true.
     */
    public FieldData(String text, FieldBoundingBox boundingBox, int pageNumber,
        final List<FormElement> fieldElements) {
        super(text, boundingBox, pageNumber);
        this.fieldElements = fieldElements == null ? null : Collections.unmodifiableList(fieldElements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldBoundingBox getBoundingBox() {
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
    public int getPageNumber() {
        return super.getPageNumber();
    }

    /**
     * When `includeFieldElements` is set to true, gets a list of reference elements constituting
     * this {@code FieldData}.
     *
     * @return The unmodifiable list of reference elements constituting this {@code FieldData}.
     */
    public List<FormElement> getFieldElements() {
        return this.fieldElements;
    }
}
