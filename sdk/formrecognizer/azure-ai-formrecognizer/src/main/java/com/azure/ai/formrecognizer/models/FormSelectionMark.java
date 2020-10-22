// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

/**
 * The FormSelectionMark model.
 */
public final class FormSelectionMark extends FormElement {
    private float confidence;
    private SelectionMarkState state;

    /**
     * Creates a FormSelectionMark.
     *
     * @param text The text content of the extracted element.
     * @param boundingBox The BoundingBox specifying relative coordinates of the element.
     * @param pageNumber the 1 based page number.
     */
    public FormSelectionMark(String text, FieldBoundingBox boundingBox, int pageNumber) {
        super(text, boundingBox, pageNumber);
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
