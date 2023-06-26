// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentImageHelper;

import java.util.List;

/** An image object detected in the page. */
public final class DocumentImage {
    /*
     * Bounding polygon of the image.
     */
    private List<Point> polygon;

    /*
     * Location of the image in the reading order concatenated content.
     */
    private DocumentSpan span;

    /*
     * 1-based page number of the page that contains the image.
     */
    private int pageNumber;

    /*
     * Confidence of correctly identifying the image.
     */
    private float confidence;

    /**
     * Get the polygon property: Bounding polygon of the image.
     *
     * @return the polygon value.
     */
    public List<Point> getBoundingPolygon() {
        return this.polygon;
    }

    /**
     * Set the polygon property: Bounding polygon of the image.
     *
     * @param polygon the polygon value to set.
     */
    void setPolygon(List<Point> polygon) {
        this.polygon = polygon;
    }

    /**
     * Get the span property: Location of the image in the reading order concatenated content.
     *
     * @return the span value.
     */
    public DocumentSpan getSpan() {
        return this.span;
    }

    /**
     * Get the pageNumber property: 1-based page number of the page that contains the image.
     *
     * @return the pageNumber value.
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * Get the confidence property: Confidence of correctly identifying the image.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    void setSpan(DocumentSpan span) {
        this.span = span;
    }

    void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentImageHelper.setAccessor(new DocumentImageHelper.DocumentImageAccessor() {
            @Override
            public void setSpan(DocumentImage documentImage, DocumentSpan span) {
                documentImage.setSpan(span);
            }

            @Override
            public void setPageNumber(DocumentImage documentImage, int pageNumber) {
                documentImage.setPageNumber(pageNumber);
            }
            @Override
            public void setConfidence(DocumentImage documentImage, float confidence) {
                documentImage.setConfidence(confidence);
            }

            @Override
            public void setBoundingPolygon(DocumentImage documentImage, List<Point> polygon) {
                documentImage.setPolygon(polygon);
            }
        });
    }
}
