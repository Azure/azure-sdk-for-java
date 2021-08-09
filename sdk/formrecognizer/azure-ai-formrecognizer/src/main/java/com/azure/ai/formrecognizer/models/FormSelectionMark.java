// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.FormSelectionMarkHelper;
import com.azure.core.annotation.Immutable;

/**
 * Represents a selection mark recognized from the input document.
 */
@Immutable
public final class FormSelectionMark extends FormElement {
    private float confidence;
    private SelectionMarkState state;

    static {
        FormSelectionMarkHelper.setAccessor(new FormSelectionMarkHelper.FormSelectionMarkAccessor() {
            @Override
            public void setConfidence(FormSelectionMark selectionMark, float confidence) {
                selectionMark.setConfidence(confidence);
            }

            @Override
            public void setState(FormSelectionMark selectionMark, SelectionMarkState state) {
                selectionMark.setState(state);
            }
        });
    }

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

    /**
     * The private setter to set the confidence property
     * via {@link FormSelectionMarkHelper.FormSelectionMarkAccessor}.
     *
     * @param confidence the confidence value for the selection mark.
     */
    private void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    /**
     * The private setter to set the state property
     * via {@link FormSelectionMarkHelper.FormSelectionMarkAccessor}.
     *
     * @param state the the state value for the selection mark.
     */
    private void setState(SelectionMarkState state) {
        this.state = state;
    }
}
