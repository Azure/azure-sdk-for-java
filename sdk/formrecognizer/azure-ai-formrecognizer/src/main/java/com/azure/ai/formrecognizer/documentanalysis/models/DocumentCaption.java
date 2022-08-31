// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.ai.formrecognizer.documentanalysis.implementation.util.DocumentCaptionHelper;
import com.azure.core.annotation.Immutable;

import java.util.List;

/** An object representing the location and content of a table caption. */
@Immutable
public final class DocumentCaption {
    /*
     * Table caption content.
     */
    private String content;

    /*
     * Bounding regions covering the table caption.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the table caption in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /**
     * Get the content property: Table caption content.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Table caption content.
     *
     * @param content the content value to set.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the table caption.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Set the boundingRegions property: Bounding regions covering the table caption.
     *
     * @param boundingRegions the boundingRegions value to set.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the table caption in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the table caption in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    static {
        DocumentCaptionHelper.setAccessor(new DocumentCaptionHelper.DocumentCaptionAccessor() {
            @Override
            public void setContent(DocumentCaption documentCaption, String content) {
                documentCaption.setContent(content);
            }

            @Override
            public void setBoundingRegions(DocumentCaption documentCaption, List<BoundingRegion> boundingRegions) {
                documentCaption.setBoundingRegions(boundingRegions);
            }

            @Override
            public void setSpans(DocumentCaption documentCaption, List<DocumentSpan> spans) {
                documentCaption.setSpans(spans);
            }
        });
    }
}
