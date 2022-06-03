// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentParagraphHelper;

import java.util.List;

/** A paragraph object consisting with contiguous lines generally with common alignment and spacing. */
public final class DocumentParagraph {
    /*
     * Semantic role of the paragraph.
     */
    private ParagraphRole role;

    /*
     * Concatenated content of the paragraph in reading order.
     */
    private String content;

    /*
     * Bounding regions covering the paragraph.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the paragraph in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /**
     * Get the role property: Semantic role of the paragraph.
     *
     * @return the role value.
     */
    public ParagraphRole getRole() {
        return this.role;
    }

    /**
     * Set the role property: Semantic role of the paragraph.
     *
     * @param role the role value to set.
     */
    void setRole(ParagraphRole role) {
        this.role = role;
    }

    /**
     * Get the content property: Concatenated content of the paragraph in reading order.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Concatenated content of the paragraph in reading order.
     *
     * @param content the content value to set.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the paragraph.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Set the boundingRegions property: Bounding regions covering the paragraph.
     *
     * @param boundingRegions the boundingRegions value to set.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the paragraph in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the paragraph in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    static {
        DocumentParagraphHelper.setAccessor(new DocumentParagraphHelper.DocumentParagraphAccessor() {
            @Override
            public void setRole(DocumentParagraph documentParagraph, ParagraphRole paragraphRole) {
                documentParagraph.setRole(paragraphRole);
            }

            @Override
            public void setBoundingRegions(DocumentParagraph documentParagraph, List<BoundingRegion> boundingRegions) {
                documentParagraph.setBoundingRegions(boundingRegions);
            }

            @Override
            public void setSpans(DocumentParagraph documentParagraph, List<DocumentSpan> spans) {
                documentParagraph.setSpans(spans);
            }

            @Override
            public void setContent(DocumentParagraph documentParagraph, String content) {
                documentParagraph.setContent(content);
            }
        });
    }
}
