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
     * Bounding box of the line.
     */
    private List<Float> boundingBox;

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
     * Get the boundingBox property: Bounding box of the line.
     *
     * @return the boundingBox value.
     */
    public List<Float> getBoundingBox() {
        return this.boundingBox;
    }

    /**
     * Set the boundingBox property: Bounding box of the line.
     *
     * @param boundingBox the boundingBox value to set.
     * @return the DocumentLine object itself.
     */
    void setBoundingBox(List<Float> boundingBox) {
        this.boundingBox = boundingBox;
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
            public void setBoundingBox(DocumentLine documentLine, List<Float> boundingBox) {
                documentLine.setBoundingBox(boundingBox);
            }

            @Override
            public void setSpans(DocumentLine documentLine, List<DocumentSpan> spans) {
                documentLine.setSpans(spans);
            }
        });
    }
}
