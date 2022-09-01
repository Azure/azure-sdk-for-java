// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentWordHelper;
import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * A word object consisting of a contiguous sequence of characters. For non-space delimited languages, such as Chinese,
 * Japanese, and Korean, each character is represented as its own word.
 */
@Immutable
public final class DocumentWord {
    /*
     * Text content of the word.
     */
    private String content;

    /*
     * Bounding box of the word.
     */
    private List<Point> boundingPolygon;

    /*
     * Location of the word in the reading order concatenated content.
     */
    private DocumentSpan span;

    /*
     * Confidence of correctly extracting the word.
     */
    private float confidence;

    /**
     * Get the content property: Text content of the word.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Text content of the word.
     *
     * @param content the content value to set.
     * @return the DocumentWord object itself.
     */
    private void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the list of coordinates of the bounding polygon for the word.
     * The numbers represent the x, y values of the polygon vertices, clockwise from the left (-180 degrees inclusive)
     * relative to the element orientation.
     *
     * @return the boundingPolygon value.
     */
    public List<Point> getBoundingPolygon() {
        return this.boundingPolygon;
    }

    /**
     * Set the list of coordinates of the bounding polygon for the word.
     * The numbers represent the x, y values of the polygon vertices, clockwise from the left (-180 degrees inclusive)
     * relative to the element orientation.
     *
     * @param boundingPolygon the boundingPolygon value to set.
     * @return the DocumentWord object itself.
     */
    private void setBoundingPolygon(List<Point> boundingPolygon) {
        this.boundingPolygon = boundingPolygon;
    }

    /**
     * Get the span property: Location of the word in the reading order concatenated content.
     *
     * @return the span value.
     */
    public DocumentSpan getSpan() {
        return this.span;
    }

    /**
     * Set the span property: Location of the word in the reading order concatenated content.
     *
     * @param span the span value to set.
     * @return the DocumentWord object itself.
     */
    private void setSpan(DocumentSpan span) {
        this.span = span;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the word.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly extracting the word.
     *
     * @param confidence the confidence value to set.
     * @return the DocumentWord object itself.
     */
    private void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentWordHelper.setAccessor(new DocumentWordHelper.DocumentWordAccessor() {
            @Override
            public void setBoundingPolygon(DocumentWord documentWord, List<Point> boundingPolygon) {
                documentWord.setBoundingPolygon(boundingPolygon);
            }

            @Override
            public void setContent(DocumentWord documentWord, String content) {
                documentWord.setContent(content);
            }

            @Override
            public void setSpan(DocumentWord documentWord, DocumentSpan span) {
                documentWord.setSpan(span);
            }

            @Override
            public void setConfidence(DocumentWord documentWord, float confidence) {
                documentWord.setConfidence(confidence);
            }
        });
    }
}
