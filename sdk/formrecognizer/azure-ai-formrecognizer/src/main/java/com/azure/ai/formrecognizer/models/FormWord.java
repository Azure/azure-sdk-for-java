// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The FormTable model.
 */
@Immutable
public final class FormWord extends FormContent {
    /*
     * The confidence value of the recognized word.
     */
    private final float confidence;

    /**
     * Creates raw OCR item.
     *  @param text The text content of ExtractedField.
     * @param boundingBox The BoundingBox of ExtractedField.
     * @param pageNumber The 1 based page number.
     * @param confidence the confidence.
     */
    public FormWord(String text, BoundingBox boundingBox, Integer pageNumber, final float confidence) {
        super(text, boundingBox, pageNumber);
        this.confidence = confidence;
    }

    /**
     * Gets the confidence property of the Form Word.
     *
     * @return The confidence property of the Form Word.
     */
    public float getConfidence() {
        return this.confidence;
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
}
