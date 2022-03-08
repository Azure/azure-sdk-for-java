// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * Represents a word recognized from the input document.
 */
@Immutable
public final class FormWord extends FormElement {

    /*
     * The confidence value of the recognized word.
     */
    private final float confidence;

    /**
     * Constructs a FormWord object.
     *
     * @param text the text content of recognized field.
     * @param boundingBox the BoundingBox of recognized field.
     * @param pageNumber the 1 based page number.
     * @param confidence the confidence property of the Form Word.
     */
    public FormWord(String text, FieldBoundingBox boundingBox, int pageNumber, final float confidence) {
        super(text, boundingBox, pageNumber);
        this.confidence = confidence;
    }

    /**
     * Gets the confidence property of the Form Word.
     *
     * @return the confidence property of the Form Word.
     */
    public float getConfidence() {
        return this.confidence;
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
}
