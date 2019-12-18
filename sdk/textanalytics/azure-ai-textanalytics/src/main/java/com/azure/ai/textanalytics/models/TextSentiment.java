// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The TextSentiment model.
 */
@Fluent
public class TextSentiment {
    private double negativeScore;
    private double neutralScore;
    private double positiveScore;
    private int length;
    private int offset;
    private TextSentimentClass textSentimentClass;

    /**
     * Get the length of the text by Unicode standard.
     *
     * @return the length of the text by Unicode standard
     */
    public int getLength() {
        return length;
    }

    /**
     * Set the length of the text by Unicode standard.
     *
     * @param length the length of the text by Unicode standard
     * @return the TextSentiment object itself
     */
    public TextSentiment setLength(int length) {
        this.length = length;
        return this;
    }

    /**
     * Get the score of negative sentiment.
     *
     * @return the score of negative sentiment
     */
    public double getNegativeScore() {
        return negativeScore;
    }

    /**
     * Set the score of negative sentiment.
     *
     * @param negativeScore the score of negative sentiment
     * @return the TextSentiment object itself
     */
    public TextSentiment setNegativeScore(double negativeScore) {
        this.negativeScore = negativeScore;
        return this;
    }

    /**
     * Get the score of neutral sentiment.
     *
     * @return the score of neutral sentiment
     */
    public double getNeutralScore() {
        return neutralScore;
    }

    /**
     * Set the score of neutral sentiment.
     *
     * @param neutralScore the score of neutral sentiment
     * @return the TextSentiment object itself
     */
    public TextSentiment setNeutralScore(double neutralScore) {
        this.neutralScore = neutralScore;
        return this;
    }

    /**
     * Get the score of positive sentiment.
     *
     * @return the score of positive sentiment
     */
    public double getPositiveScore() {
        return positiveScore;
    }

    /**
     * Set the score of positive sentiment.
     *
     * @param positiveScore the score of positive sentiment
     * @return the TextSentiment object itself
     */
    public TextSentiment setPositiveScore(double positiveScore) {
        this.positiveScore = positiveScore;
        return this;
    }

    /**
     * Get the offset of the text sentiment.
     *
     * @return the offset of text sentiment
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Set the offset of the text sentiment.
     *
     * @param offset the offset of text sentiment
     * @return the TextSentiment object itself
     */
    public TextSentiment setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Get the text sentiment enum class: POSITIVE, NEGATIVE, NEUTRAL, MIXED.
     *
     * @return the TextSentimentClass
     */
    public TextSentimentClass getTextSentimentClass() {
        return textSentimentClass;
    }

    /**
     * Set the text sentiment enum class: POSITIVE, NEGATIVE, NEUTRAL, MIXED.
     *
     * @param textSentimentClass the TextSentimentClass
     * @return the TextSentiment object itself
     */
    public TextSentiment setTextSentimentClass(TextSentimentClass textSentimentClass) {
        this.textSentimentClass = textSentimentClass;
        return this;
    }
}
