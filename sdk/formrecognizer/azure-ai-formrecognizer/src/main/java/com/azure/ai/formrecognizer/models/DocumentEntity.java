// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentEntityHelper;

import java.util.List;

/**
 * An object representing various categories of entities.
 */
public final class DocumentEntity {
    /*
     * Entity type.
     */
    private String category;

    /*
     * Entity sub type.
     */
    private String subCategory;

    /*
     * Entity content.
     */
    private String content;

    /*
     * Bounding regions covering the entity.
     */
    private List<BoundingRegion> boundingRegions;

    /*
     * Location of the entity in the reading order concatenated content.
     */
    private List<DocumentSpan> spans;

    /*
     * Confidence of correctly extracting the entity.
     */
    private float confidence;

    /**
     * Get the category property: Entity type.
     *
     * @return the category value.
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Set the category property: Entity type.
     *
     * @param category the category value to set.
     * @return the DocumentEntity object itself.
     */
    void setCategory(String category) {
        this.category = category;
    }

    /**
     * Get the subCategory property: Entity sub type.
     *
     * @return the subCategory value.
     */
    public String getSubCategory() {
        return this.subCategory;
    }

    /**
     * Set the subCategory property: Entity sub type.
     *
     * @param subCategory the subCategory value to set.
     * @return the DocumentEntity object itself.
     */
    void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    /**
     * Get the content property: Entity content.
     *
     * @return the content value.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Set the content property: Entity content.
     *
     * @param content the content value to set.
     * @return the DocumentEntity object itself.
     */
    void setContent(String content) {
        this.content = content;
    }

    /**
     * Get the boundingRegions property: Bounding regions covering the entity.
     *
     * @return the boundingRegions value.
     */
    public List<BoundingRegion> getBoundingRegions() {
        return this.boundingRegions;
    }

    /**
     * Set the boundingRegions property: Bounding regions covering the entity.
     *
     * @param boundingRegions the boundingRegions value to set.
     * @return the DocumentEntity object itself.
     */
    void setBoundingRegions(List<BoundingRegion> boundingRegions) {
        this.boundingRegions = boundingRegions;
    }

    /**
     * Get the spans property: Location of the entity in the reading order concatenated content.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the entity in the reading order concatenated content.
     *
     * @param spans the spans value to set.
     * @return the DocumentEntity object itself.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    /**
     * Get the confidence property: Confidence of correctly extracting the entity.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly extracting the entity.
     *
     * @param confidence the confidence value to set.
     * @return the DocumentEntity object itself.
     */
    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentEntityHelper.setAccessor(new DocumentEntityHelper.DocumentEntityAccessor() {

            @Override
            public void setCategory(DocumentEntity documentEntity, String category) {
                documentEntity.setCategory(category);
            }

            @Override
            public void setSubCategory(DocumentEntity documentEntity, String subCategory) {
                documentEntity.setCategory(subCategory);
            }

            @Override
            public void setContent(DocumentEntity documentEntity, String content) {
                documentEntity.setContent(content);
            }

            @Override
            public void setBoundingRegions(DocumentEntity documentEntity, List<BoundingRegion> boundingRegion) {
                documentEntity.setBoundingRegions(boundingRegion);
            }

            @Override
            public void setSpans(DocumentEntity documentEntity, List<DocumentSpan> spans) {
                documentEntity.setSpans(spans);
            }

            @Override
            public void setConfidence(DocumentEntity documentEntity, Float confidence) {
                documentEntity.setConfidence(confidence);
            }
        });
    }
}
