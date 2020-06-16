// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The FieldText model.
 */
@Immutable
public final class FieldText extends FormContent {

    /**
     * The list of text element references for the field value.
     */
    private final List<FormContent> textContent;

    /**
     * Creates raw OCR FieldText item.
     *
     * @param text The text content of ExtractedField.
     * @param boundingBox The BoundingBox of ExtractedField.
     * @param pageNumber the 1 based page number.
     * @param textContent The list of text element references when includeTextDetails is set to true.
     */
    public FieldText(String text, BoundingBox boundingBox, Integer pageNumber,
                     final List<FormContent> textContent) {
        super(text, boundingBox, pageNumber);
        this.textContent = textContent;
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
    public Integer getPageNumber() {
        return super.getPageNumber();
    }

    /**
     * Gets the list of reference text elements.
     *
     * @return The list of reference elements.
     */
    public List<FormContent> getTextContent() {
        return this.textContent;
    }
}
