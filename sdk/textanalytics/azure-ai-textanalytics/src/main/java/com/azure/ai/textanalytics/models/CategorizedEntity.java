// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link CategorizedEntity} model.
 */
@Immutable
public final class CategorizedEntity {
    /*
     * CategorizedEntity text as appears in the request.
     */
    private final String text;

    /*
     * CategorizedEntity category, such as Person/Location/Org/SSN etc
     */
    private final EntityCategory category;

    /*
     * CategorizedEntity sub category, such as Age/Year/TimeRange etc
     */
    private final String subcategory;

    /*
     * Confidence score between 0 and 1 of the extracted entity.
     */
    private final double confidenceScore;

    /**
     * Creates a {@link CategorizedEntity} model that describes entity.
     *
     * @param text The entity text as appears in the request.
     * @param category The entity category, such as Person/Location/Org/SSN etc.
     * @param subcategory The entity subcategory, such as Age/Year/TimeRange etc.
     * @param confidenceScore A confidence score between 0 and 1 of the extracted entity.
     */
    public CategorizedEntity(String text, EntityCategory category, String subcategory, double confidenceScore) {
        this.text = text;
        this.category = category;
        this.subcategory = subcategory;
        this.confidenceScore = confidenceScore;
    }

    /**
     * Get the text property: Categorized entity text as appears in the request.
     *
     * @return The text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the category property: Categorized entity category, such as Person/Location/Org/SSN etc.
     *
     * @return The category value.
     */
    public EntityCategory getCategory() {
        return this.category;
    }

    /**
     * Get the subcategory property: Categorized entity sub category, such as Age/Year/TimeRange etc.
     *
     * @return The subcategory value.
     */
    public String getSubcategory() {
        return this.subcategory;
    }

    /**
     * Get the score property: Confidence score between 0 and 1 of the extracted entity.
     *
     * @return The score value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }
}
