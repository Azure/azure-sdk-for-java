// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Representation of the moderation results for the list of categories that OpenAI checks the input for.
 */
@Immutable
public class ModerationResults {
    /*
     * If the input has at least one category that violates the OpenAI usage policies.
     */
    @Generated
    @JsonProperty(value = "flagged")
    private Boolean flagged;

    /*
     * A set of policy categories along with a boolean to indicate if that particular category
     * is violated.
     */
    @Generated
    @JsonProperty(value = "categories")
    private Map<String, Boolean> categories;

    /*
     * A set of policy categories along with a number to represent a score in the models confidence
     * that a policy has been violated between 0 and 1, with higher numbers denoting more confidence.
     */
    @Generated
    @JsonProperty(value = "category_scores")
    private Map<String, Double> categoryScores;

    /**
     * Creates a ModerationResult instance
     * @param flagged the flagged value
     * @param categories the categories value
     * @param categoryScores the categoriesScore value
     */
    @Generated
    @JsonCreator
    public ModerationResults(@JsonProperty(value = "flagged") Boolean flagged,
                             @JsonProperty(value = "categories") Map<String, Boolean> categories,
                             @JsonProperty(value = "category_scores") Map<String, Double> categoryScores) {
        this.flagged = flagged;
        this.categories = categories;
        this.categoryScores = categoryScores;
    }

    /**
     * If the input has at least one category that violates one of OpenAI's policies
     *
     * @return the flagged value
     */
    @Generated
    public Boolean getFlagged() {
        return flagged;
    }

    /**
     * A set of policy categories along with a boolean to indicate if that particular category
     * is violated.
     *
     * @return the categories value
     */
    @Generated
    public Map<String, Boolean> getCategories() {
        return categories;
    }

    /**
     * A set of policy categories along with a number to represent a score in the models confidence
     * that a policy has been violated between 0 and 1, with higher numbers denoting more confidence.
     *
     * @return the categories scores
     */
    @Generated
    public Map<String, Double> getCategoryScores() {
        return categoryScores;
    }
}
