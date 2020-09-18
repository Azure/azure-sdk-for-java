// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The SelectionMark model.
 */
@Immutable
public final class FormSelectionMark extends FormElement {
    private final float confidence;
    private final SelectionMarkState state;

    /**
     *
     * @param text the text content of ExtractedField.
     * @param boundingBox the Bounding Box of the recognized field.
     * @param pageNumber the 1 based page number.
     * @param state the state of selection mark.
     * @param confidence the confidence of selection mark.
     */
    public FormSelectionMark(String text, FieldBoundingBox boundingBox, int pageNumber, SelectionMarkState state,
        float confidence) {
        super(text, boundingBox, pageNumber);
        this.state = state;
        this.confidence = confidence;
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
     * Gets the state property of the selection mark.
     *
     * @return the state property of the selection mark.
     */
    public SelectionMarkState getState() {
        return this.state;
    }

    /**
     * Gets the confidence property of the selection mark.
     *
     * @return the confidence property of the selection mark.
     */
    public float getConfidence() {
        return this.confidence;
    }
}
