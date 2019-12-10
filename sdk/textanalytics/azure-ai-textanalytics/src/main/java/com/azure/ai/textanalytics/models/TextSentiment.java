// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The TextSentiment model.
 */
@Fluent
public class TextSentiment {

    // SentimentConfidenceScorePerLabel
    private double negativeScore;

    private double neutralScore;

    private double positiveScore;

    // sentence
    private String length;

    private int offset;

    // sentiment string
    private TextSentimentClass textSentimentClass;

    public String getLength() {
        return length;
    }

    TextSentiment setLength(String length) {
        this.length = length;
        return this;
    }

    public double getNegativeScore() {
        return negativeScore;
    }

    TextSentiment setNegativeScore(double negativeScore) {
        this.negativeScore = negativeScore;
        return this;
    }

    public double getNeutralScore() {
        return neutralScore;
    }

    TextSentiment setNeutralScore(double neutralScore) {
        this.neutralScore = neutralScore;
        return this;
    }

    public double getPositiveScore() {
        return positiveScore;
    }

    TextSentiment setPositiveScore(double positiveScore) {
        this.positiveScore = positiveScore;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    TextSentiment setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public TextSentimentClass getTextSentimentClass() {
        return textSentimentClass;
    }

    TextSentiment setTextSentimentClass(TextSentimentClass textSentimentClass) {
        this.textSentimentClass = textSentimentClass;
        return this;
    }
}
