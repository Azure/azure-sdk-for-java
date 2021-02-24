// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareEntityPropertiesHelper;
import com.azure.core.util.IterableStream;

import java.util.Collections;
import java.util.Map;

/**
 * The {@link HealthcareEntity} model.
 */
public final class HealthcareEntity {
    private String text;
    private String category;
    private String subcategory;
    private double confidenceScore;
    private int offset;
    private int length;
    private boolean negated;
    private IterableStream<EntityDataSource> dataSources;
    private Map<HealthcareEntity, HealthcareEntityRelationType> relatedEntities;

    static {
        HealthcareEntityPropertiesHelper.setAccessor(new HealthcareEntityPropertiesHelper.HealthcareEntityAccessor() {
            @Override
            public void setText(HealthcareEntity healthcareEntity, String text) {
                healthcareEntity.setText(text);
            }

            @Override
            public void setCategory(HealthcareEntity healthcareEntity, String category) {
                healthcareEntity.setCategory(category);
            }

            @Override
            public void setSubcategory(HealthcareEntity healthcareEntity, String subcategory) {
                healthcareEntity.setSubcategory(subcategory);
            }

            @Override
            public void setConfidenceScore(HealthcareEntity healthcareEntity, double confidenceScore) {
                healthcareEntity.setConfidenceScore(confidenceScore);
            }

            @Override
            public void setOffset(HealthcareEntity healthcareEntity, int offset) {
                healthcareEntity.setOffset(offset);
            }

            @Override
            public void setLength(HealthcareEntity healthcareEntity, int length) {
                healthcareEntity.setLength(length);
            }

            @Override
            public void setNegated(HealthcareEntity healthcareEntity, boolean negated) {
                healthcareEntity.setNegated(negated);
            }

            @Override
            public void setDataSources(HealthcareEntity healthcareEntity,
                IterableStream<EntityDataSource> dataSources) {
                healthcareEntity.setDataSources(dataSources);
            }

            @Override
            public void setRelatedEntities(HealthcareEntity healthcareEntity,
                Map<HealthcareEntity, HealthcareEntityRelationType> relatedEntities) {
                healthcareEntity.setRelatedEntities(relatedEntities);
            }
        });
    }

    /**
     * Get the text property: Healthcare entity text as appears in the request.
     *
     * @return The text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the category property: Healthcare entity category, such as Person/Location/Org/SSN etc.
     *
     * @return The category value.
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Get the subcategory property: Healthcare entity subcategory, such as DateTime etc.
     *
     * @return The subcategory value.
     */
    public String getSubcategory() {
        return this.subcategory;
    }

    /**
     * Get the score property: If a well-known item is recognized, a decimal
     * number denoting the confidence level between 0 and 1 will be returned.
     *
     * @return The score value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }

    /**
     * Get the offset of entity text. The start position for the entity text in a document.
     *
     * @return The offset of entity text.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the length of entity text.
     *
     * @return The length of entity text.
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the isNegated property: The isNegated property.
     *
     * @return the isNegated value.
     */
    public boolean isNegated() {
        return this.negated;
    }

    /**
     * Get the healthcare entity data sources property: Entity references in known data sources.
     *
     * @return the dataSources value.
     */
    public IterableStream<EntityDataSource> getDataSources() {
        return this.dataSources;
    }

    /**
     * Get the related healthcare entities and relation type.
     *
     * @return the related healthcare entities and relation type.
     */
    public Map<HealthcareEntity, HealthcareEntityRelationType> getRelatedEntities() {
        return Collections.unmodifiableMap(relatedEntities);
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setCategory(String category) {
        this.category = category;
    }

    private void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    private void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void setLength(int length) {
        this.length = length;
    }

    private void setNegated(boolean negated) {
        this.negated = negated;
    }

    private void setDataSources(IterableStream<EntityDataSource> dataSources) {
        this.dataSources = dataSources;
    }

    private void setRelatedEntities(Map<HealthcareEntity, HealthcareEntityRelationType> relatedEntities) {
        this.relatedEntities = relatedEntities;
    }
}
