// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * The {@link SentimentConfidenceScores} model
 */
public interface SentimentConfidenceScores {
    /**
     * The negative score value, range in between 0 and 1.0.
     *
     * @return the negative score value.
     */
    double getNegative();

    /**
     * The neutral score value, range in between 0 and 1.0.
     *
     * @return The neutral score value.
     */
    double getNeutral();

    /**
     * The positive score value, range in between 0 and 1.0.
     *
     * @return The positive score value.
     */
    double getPositive();
}
