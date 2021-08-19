// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareEntityPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link HealthcareEntity} model.
 */
@Immutable
public final class HealthcareEntity {
    private String text;
    private String normalizedText;
    private HealthcareEntityCategory category;
    private String subcategory;
    private double confidenceScore;
    private int offset;
    private int length;
    private IterableStream<EntityDataSource> dataSources;
    private HealthcareEntityAssertion assertion;

    static {
        HealthcareEntityPropertiesHelper.setAccessor(new HealthcareEntityPropertiesHelper.HealthcareEntityAccessor() {
            @Override
            public void setText(HealthcareEntity healthcareEntity, String text) {
                healthcareEntity.setText(text);
            }

            @Override
            public void setNormalizedText(HealthcareEntity healthcareEntity, String normalizedText) {
                healthcareEntity.setNormalizedText(normalizedText);
            }

            @Override
            public void setCategory(HealthcareEntity healthcareEntity, HealthcareEntityCategory category) {
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
            public void setDataSources(HealthcareEntity healthcareEntity,
                IterableStream<EntityDataSource> dataSources) {
                healthcareEntity.setDataSources(dataSources);
            }

            @Override
            public void setAssertion(HealthcareEntity healthcareEntity, HealthcareEntityAssertion assertion) {
                healthcareEntity.setAssertion(assertion);
            }
        });
    }

    /**
     * Gets the text property: Healthcare entity text as appears in the request.
     *
     * @return The text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets the normalized text property: The normalized text is preferred name for the entity.
     * Example: 'histologically' would have a 'name' of 'histologic'.
     *
     * @return The normalized text value.
     */
    public String getNormalizedText() {
        return this.normalizedText;
    }

    /**
     * Gets the category property: Healthcare entity category, such as Person/Location/Org/SSN etc.
     *
     * @return The category value.
     */
    public HealthcareEntityCategory getCategory() {
        return this.category;
    }

    /**
     * Gets the subcategory property: Healthcare entity subcategory, such as DateTime etc.
     *
     * @return The subcategory value.
     */
    public String getSubcategory() {
        return this.subcategory;
    }

    /**
     * Gets the score property: If a well-known item is recognized, a decimal
     * number denoting the confidence level between 0 and 1 will be returned.
     *
     * @return The score value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }

    /**
     * Gets the offset of entity text. The start position for the entity text in a document.
     *
     * @return The offset of entity text.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the length of entity text.
     *
     * @return The length of entity text.
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the healthcare entity data sources property: Entity references in known data sources.
     *
     * @return the dataSources value.
     */
    public IterableStream<EntityDataSource> getDataSources() {
        return this.dataSources;
    }

    /**
     * Gets the assertion property.
     *
     * @return the assertion property.
     */
    public HealthcareEntityAssertion getAssertion() {
        return this.assertion;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setNormalizedText(String normalizedText) {
        this.normalizedText = normalizedText;
    }

    private void setCategory(HealthcareEntityCategory category) {
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

    private void setDataSources(IterableStream<EntityDataSource> dataSources) {
        this.dataSources = dataSources;
    }

    private void setAssertion(HealthcareEntityAssertion assertion) {
        this.assertion = assertion;
    }
}
