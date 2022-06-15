// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentLineHelper;

import java.util.List;

/**
 * A content line object consisting of an adjacent sequence of content elements, such as words and selection marks.
 */
public final class DocumentLine {
    /*
     * Concatenated content of the contained elements in reading order.
     */
    private String content;

    /*
     * Bounding polygon of the line.
     */
    private List<Point> boundingPolygon;

    /*
     * Location of the line in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /**
     * Get the content property: Concatenated content of the contained elements in reading order.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Concatenated content of the contained elements in reading order.
     *
     * @param content the content value to set.
     * @return the DocumentLine object itself.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the list of coordinates of the bounding polygon for the line.
     * The numbers represent the x, y values of the polygon vertices, clockwise from the left (-180 degrees inclusive)
     * relative to the element orientation.
     *
     * @return the boundingPolygon value.
     */
    public List<Point> getBoundingPolygon() {
        return this.boundingPolygon;
    }

    /**
     * Set the list of coordinates of the bounding polygon for the line.
     * The numbers represent the x, y values of the polygon vertices, clockwise from the left (-180 degrees inclusive)
     * relative to the element orientation.
     *
     * @param boundingPolygon the boundingPolygon value to set.
     * @return the DocumentLine object itself.
     */
    void setBoundingPolygon(List<Point> boundingPolygon) {
        this.boundingPolygon = boundingPolygon;
    }

    /**
     * Get the spans property: Location of the line in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the line in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the DocumentLine object itself.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    static {
        DocumentLineHelper.setAccessor(new DocumentLineHelper.DocumentLineAccessor() {
            @Override
            public void setContent(DocumentLine documentLine, String content) {
                documentLine.setContent(content);
            }

            @Override
            public void setBoundingPolygon(DocumentLine documentLine, List<Point> boundingPolygon) {
                documentLine.setBoundingPolygon(boundingPolygon);
            }

            @Override
            public void setSpans(DocumentLine documentLine, List<DocumentSpan> spans) {
                documentLine.setSpans(spans);
            }
        });
    }
}
