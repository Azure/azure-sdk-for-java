// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The Sentiment model.
 */
@Fluent
public class Sentiment {

    // SentimentConfidenceScorePerLabel
    private double negativeScore;

    private double neutralScore;

    private double positiveScore;

    // sentence
    private String length;

    private int offSet;

    // sentiment string
    private SentimentClass sentimentClass;

    public String getLength() {
        return length;
    }

    public Sentiment setLength(String length) {
        this.length = length;
        return this;
    }

    public double getNegativeScore() {
        return negativeScore;
    }

    public Sentiment setNegativeScore(double negativeScore) {
        this.negativeScore = negativeScore;
        return this;
    }

    public double getNeutralScore() {
        return neutralScore;
    }

    public Sentiment setNeutralScore(double neutralScore) {
        this.neutralScore = neutralScore;
        return this;
    }

    public double getPositiveScore() {
        return positiveScore;
    }

    public Sentiment setPositiveScore(double positiveScore) {
        this.positiveScore = positiveScore;
        return this;
    }

    public int getOffSet() {
        return offSet;
    }

    public Sentiment setOffSet(int offSet) {
        this.offSet = offSet;
        return this;
    }

    public SentimentClass getSentimentClass() {
        return sentimentClass;
    }

    public Sentiment setSentimentClass(SentimentClass sentimentClass) {
        this.sentimentClass = sentimentClass;
        return this;
    }
}
