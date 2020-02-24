// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The SentimentConfidenceScorePerLabel model
 */
@Immutable
public final class SentimentConfidenceScorePerLabel {
    private final double negativeScore;
    private final double neutralScore;
    private final double positiveScore;

    /**
     * Creates a {@code SentimentConfidenceScorePerLabel} model that describes the sentiment score of the sentiment
     * label.
     *
     * @param negativeScore negative score value, range in between 0 and 1.0
     * @param neutralScore neutral score value, range in between 0 and 1.0
     * @param positiveScore positive score value, range in between 0 and 1.0
     */
    public SentimentConfidenceScorePerLabel(double negativeScore, double neutralScore, double positiveScore) {
        this.negativeScore = negativeScore;
        this.neutralScore = neutralScore;
        this.positiveScore = positiveScore;
    }

    /**
     * The negative score value, range in between 0 and 1.0.
     *
     * @return negative score value
     */
    public double getNegative() {
        return negativeScore;
    }

    /**
     * The neutral score value, range in between 0 and 1.0.
     *
     * @return neutral score value
     */
    public double getNeutral() {
        return neutralScore;
    }

    /**
     * The positive score value, range in between 0 and 1.0.
     *
     * @return positive score value
     */
    public double getPositive() {
        return positiveScore;
    }
}
