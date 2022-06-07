// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentSelectionMarkHelper;

import java.util.List;

/**
 * A selection mark object representing check boxes, radio buttons, and other elements indicating a selection.
 */
public final class DocumentSelectionMark {
    /*
     * State of the selection mark.
     */
    private SelectionMarkState state;

    /*
     * Bounding polygon of the selection mark.
     */
    private List<Point> boundingPolygon;

    /*
     * Location of the selection mark in the reading order concatenated
     * content.
     */
    private DocumentSpan span;

    /*
     * Confidence of correctly extracting the selection mark.
     */
    private float confidence;

    /**
     * Get the state property: State of the selection mark.
     *
     * @return the state value.
     */
    public SelectionMarkState getState() {
        return this.state;
    }

    /**
     * Set the state property: State of the selection mark.
     *
     * @param state the state value to set.
     * @return the DocumentSelectionMark object itself.
     */
    void setState(SelectionMarkState state) {
        this.state = state;
    }

    /**
     * Get the list of coordinates of the bounding polygon for the selection mark.
     * The numbers represent the x, y values of the polygon vertices, clockwise from the left (-180 degrees inclusive)
     * relative to the element orientation.
     *
     * @return the boundingPolygon value.
     */
    public List<Point> getBoundingPolygon() {
        return this.boundingPolygon;
    }

    /**
     * Set the list of coordinates of the bounding polygon for the selection mark.
     * The numbers represent the x, y values of the polygon vertices, clockwise from the left (-180 degrees inclusive)
     * relative to the element orientation.
     *
     * @param boundingPolygon the boundingPolygon value to set.
     * @return the DocumentSelectionMark object itself.
     */
    void setBoundingPolygon(List<Point> boundingPolygon) {
        this.boundingPolygon = boundingPolygon;
    }

    /**
     * Get the span property: Location of the selection mark in the reading order concatenated content.
     *
     * @return the span value.
     */
    public DocumentSpan getSpan() {
        return this.span;
    }

    /**
     * Set the span property: Location of the selection mark in the reading order concatenated content.
     *
     * @param span the span value to set.
     * @return the DocumentSelectionMark object itself.
     */
    void setSpan(DocumentSpan span) {
        this.span = span;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the selection mark.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly extracting the selection mark.
     *
     * @param confidence the confidence value to set.
     * @return the DocumentSelectionMark object itself.
     */
    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentSelectionMarkHelper.setAccessor(new DocumentSelectionMarkHelper.DocumentSelectionMarkAccessor() {
            @Override
            public void setState(DocumentSelectionMark documentSelectionMark, SelectionMarkState state) {
                documentSelectionMark.setState(state);
            }

            @Override
            public void setBoundingPolygon(DocumentSelectionMark documentSelectionMark, List<Point> boundingPolygon) {
                documentSelectionMark.setBoundingPolygon(boundingPolygon);
            }

            @Override
            public void setSpan(DocumentSelectionMark documentSelectionMark, DocumentSpan span) {
                documentSelectionMark.setSpan(span);
            }

            @Override
            public void setConfidence(DocumentSelectionMark documentSelectionMark, float confidence) {
                documentSelectionMark.setConfidence(confidence);
            }
        });
    }
}
