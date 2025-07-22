// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * The SentimentAnalysisResult model.
 */
@Fluent
public class SentimentAnalysisResult {
    /*
     * Gets or sets the value of the sentiment detected (positive, negative,
     * neutral, mixed).
     */

    private String sentiment;

    /**
     * Creates an instance of SentimentAnalysisResult class.
     */
    public SentimentAnalysisResult() {
    }

    /**
     * Get the sentiment property: Gets or sets the value of the sentiment detected
     * (positive, negative, neutral,
     * mixed).
     * 
     * @return the sentiment value.
     */
    public String getSentiment() {
        return this.sentiment;
    }

    /**
     * Set the sentiment property: Gets or sets the value of the sentiment detected
     * (positive, negative, neutral,
     * mixed).
     * 
     * @param sentiment the sentiment value to set.
     * @return the SentimentAnalysisResult object itself.
     */
    public SentimentAnalysisResult setSentiment(String sentiment) {
        this.sentiment = sentiment;
        return this;
    }
}
