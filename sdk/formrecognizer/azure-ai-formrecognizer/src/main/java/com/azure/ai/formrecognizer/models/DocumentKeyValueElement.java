// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentKeyValueElementHelper;

import java.util.List;

/**
 * An object representing the field key or value in a key-value pair.
 */
public final class DocumentKeyValueElement {
    /*
     * Concatenated content of the key-value element in reading order.
     */
    private String content;

    /*
     * Bounding regions covering the key-value element.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the key-value element in the reading order concatenated
     * content.
     */
    private List<DocumentSpan> spans;

    /**
     * Get the content property: Concatenated content of the key-value element in reading order.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Concatenated content of the key-value element in reading order.
     *
     * @param content the content value to set.
     * @return the DocumentKeyValueElement object itself.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the key-value element.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Set the boundingRegions property: Bounding regions covering the key-value element.
     *
     * @param boundingRegions the boundingRegions value to set.
     * @return the DocumentKeyValueElement object itself.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the key-value element in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the key-value element in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the DocumentKeyValueElement object itself.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    static {
        DocumentKeyValueElementHelper.setAccessor(new DocumentKeyValueElementHelper.DocumentKeyValueElementAccessor() {
            @Override
            public void setContent(DocumentKeyValueElement documentKeyValueElement, String content) {
                documentKeyValueElement.setContent(content);
            }

            @Override
            public void setBoundingRegions(DocumentKeyValueElement documentKeyValueElement,
                                           List<BoundingRegion> boundingRegions) {
                documentKeyValueElement.setBoundingRegions(boundingRegions);
            }

            @Override
            public void setSpans(DocumentKeyValueElement documentKeyValueElement, List<DocumentSpan> spans) {
                documentKeyValueElement.setSpans(spans);
            }
        });
    }
}
