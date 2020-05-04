// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * The {@link CategorizedEntity} model.
 */
public interface CategorizedEntity {
    /**
     * Get the text property: Categorized entity text as appears in the request.
     *
     * @return The text value.
     */
    String getText();

    /**
     * Get the category property: Categorized entity category, such as Person/Location/Org/SSN etc.
     *
     * @return The category value.
     */
    EntityCategory getCategory();

    /**
     * Get the subcategory property: Categorized entity sub category, such as Age/Year/TimeRange etc.
     *
     * @return The subcategory value.
     */
    String getSubcategory();

    /**
     * Get the score property: Confidence score between 0 and 1 of the extracted entity.
     *
     * @return The score value.
     */
    double getConfidenceScore();
}
