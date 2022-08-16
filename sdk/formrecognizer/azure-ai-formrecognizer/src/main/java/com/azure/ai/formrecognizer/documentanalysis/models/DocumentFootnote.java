// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentFootnoteHelper;

import java.util.List;

/** An object representing the location and content of a table footnote. */
public final class DocumentFootnote {
    /*
     * Table footnote content.
     */
    private String content;

    /*
     * Bounding regions covering the table footnote.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the table footnote in the reading order concatenated
     * content.
     */
    private List<DocumentSpan> spans;

    /**
     * Get the content property: Table footnote content.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Table footnote content.
     *
     * @param content the content value to set.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the table footnote.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Set the boundingRegions property: Bounding regions covering the table footnote.
     *
     * @param boundingRegions the boundingRegions value to set.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the table footnote in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the table footnote in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    static {
        DocumentFootnoteHelper.setAccessor(new DocumentFootnoteHelper.DocumentFootnoteAccessor() {
            @Override
            public void setContent(DocumentFootnote documentFootnote, String content) {
                documentFootnote.setContent(content);
            }

            @Override
            public void setBoundingRegions(DocumentFootnote documentFootnote, List<BoundingRegion> boundingRegions) {
                documentFootnote.setBoundingRegions(boundingRegions);
            }

            @Override
            public void setSpans(DocumentFootnote documentFootnote, List<DocumentSpan> spans) {
                documentFootnote.setSpans(spans);
            }
        });
    }
}
