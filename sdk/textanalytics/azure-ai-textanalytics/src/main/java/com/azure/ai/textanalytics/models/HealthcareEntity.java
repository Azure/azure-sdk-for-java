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
    private EntityCategory category;
    private double confidenceScore;
    private int offset;
    private boolean negated;
    private IterableStream<HealthcareEntityDataSource> healthcareEntityDataSources;
    private Map<HealthcareEntity, HealthcareEntityRelationType> relatedHealthcareEntities;

    static {
        HealthcareEntityPropertiesHelper.setAccessor(new HealthcareEntityPropertiesHelper.HealthcareEntityAccessor() {
            @Override
            public void setText(HealthcareEntity healthcareEntity, String text) {
                healthcareEntity.setText(text);
            }

            @Override
            public void setCategory(HealthcareEntity healthcareEntity, EntityCategory category) {
                healthcareEntity.setCategory(category);
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
            public void setNegated(HealthcareEntity healthcareEntity, boolean negated) {
                healthcareEntity.setNegated(negated);
            }

            @Override
            public void setHealthcareEntityDataSources(HealthcareEntity healthcareEntity,
                IterableStream<HealthcareEntityDataSource> healthcareEntityDataSources) {
                healthcareEntity.setHealthcareEntityDataSources(healthcareEntityDataSources);
            }

            @Override
            public void setRelatedHealthcareEntities(HealthcareEntity healthcareEntity,
                Map<HealthcareEntity, HealthcareEntityRelationType> relatedHealthcareEntities) {
                healthcareEntity.setRelatedHealthcareEntities(relatedHealthcareEntities);
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
    public EntityCategory getCategory() {
        return this.category;
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
     * @return the healthcareEntityDataSources value.
     */
    public IterableStream<HealthcareEntityDataSource> getHealthcareEntityDataSources() {
        return this.healthcareEntityDataSources;
    }

    /**
     * Get the related healthcare entities and relation type.
     *
     * @return the related healthcare entities and relation type.
     */
    public Map<HealthcareEntity, HealthcareEntityRelationType> getRelatedHealthcareEntities() {
        return Collections.unmodifiableMap(relatedHealthcareEntities);
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setCategory(EntityCategory category) {
        this.category = category;
    }

    private void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void setNegated(boolean negated) {
        this.negated = negated;
    }

    private void setHealthcareEntityDataSources(
        IterableStream<HealthcareEntityDataSource> healthcareEntityDataSources) {
        this.healthcareEntityDataSources = healthcareEntityDataSources;
    }

    private void setRelatedHealthcareEntities(
        Map<HealthcareEntity, HealthcareEntityRelationType> relatedHealthcareEntities) {
        this.relatedHealthcareEntities = relatedHealthcareEntities;
    }
}
