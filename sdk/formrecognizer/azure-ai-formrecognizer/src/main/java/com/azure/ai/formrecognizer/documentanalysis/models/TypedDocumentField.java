// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import java.util.List;

/**
 * Strongly typed version of {@link DocumentField}
 * @param <T> the type of value.
 */
public class TypedDocumentField<T> {
    private T value;
    private DocumentFieldType type;
    private String content;
    private List<BoundingRegion> boundingRegions;
    private List<DocumentSpan> spans;
    private Float confidence;

    /**
     * Get value of the field.
     * @return the value of the field
     */
    public T getValue() {
        return value;
    }

    /**
     * Get the data type of the field value.
     *
     * @return the type value.
     */
    public DocumentFieldType getType() {
        return this.type;
    }

    /**
     * Get the field content.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Get the bounding regions covering the field.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Get the location of the field in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Get the confidence of correctly extracting the field.
     *
     * @return the confidence value.
     */
    public Float getConfidence() {
        return this.confidence;
    }
}
