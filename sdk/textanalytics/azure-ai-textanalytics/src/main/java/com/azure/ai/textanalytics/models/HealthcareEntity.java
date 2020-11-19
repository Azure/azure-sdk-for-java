// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareEntityPropertiesHelper;

import java.util.List;

/**
 * The {@link HealthcareEntity} model.
 */
public final class HealthcareEntity {
    private String text;
    private EntityCategory category;
    private String subcategory;
    private double confidenceScore;
    private int offset;
    private boolean negated;
    private List<HealthcareEntityLink> healthcareEntityLinks;

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
            public void setNegated(HealthcareEntity healthcareEntity, boolean negated) {
                healthcareEntity.setNegated(negated);
            }

            @Override
            public void setHealthcareEntityLinks(HealthcareEntity healthcareEntity,
                List<HealthcareEntityLink> healthcareEntityLinks) {
                healthcareEntity.setHealthcareEntityLinks(healthcareEntityLinks);
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
     * Get the subcategory property: Healthcare entity sub category, such as Age/Year/TimeRange etc.
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
     * Get the isNegated property: The isNegated property.
     *
     * @return the isNegated value.
     */
    public boolean isNegated() {
        return this.negated;
    }

    /**
     * Get the links property: Entity references in known data sources.
     *
     * @return the links value.
     */
    public List<HealthcareEntityLink> getDataSourceEntityLinks() {
        return this.healthcareEntityLinks;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setCategory(EntityCategory category) {
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

    private void setNegated(boolean negated) {
        this.negated = negated;
    }

    private void setHealthcareEntityLinks(List<HealthcareEntityLink> healthcareEntityLinks) {
        this.healthcareEntityLinks = healthcareEntityLinks;
    }
}
