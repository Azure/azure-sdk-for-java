// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The TextSentiment model.
 */
@Immutable
public final class TextSentiment {
    private final double negativeScore;
    private final double neutralScore;
    private final double positiveScore;
    private final int length;
    private final int offset;
    private final TextSentimentClass textSentimentClass;

    /**
     * Creates a {@code TextSentiment}  model that describes the sentiment analysis of text
     *
     * @param textSentimentClass text sentiment class of text
     * @param negativeScore negative score value, range in between 0 and 1.0
     * @param neutralScore neutral score value, range in between 0 and 1.0
     * @param positiveScore positive score value, range in between 0 and 1.0
     * @param length length of the text
     * @param offset the offset from the start of the document
     */
    public TextSentiment(TextSentimentClass textSentimentClass, Double negativeScore, Double neutralScore,
                         Double positiveScore, int length, int offset) {
        this.negativeScore = negativeScore == null ? 0.0 : negativeScore;
        this.neutralScore = neutralScore == null ? 0.0 : neutralScore;
        this.positiveScore = positiveScore == null ? 0.0 : positiveScore;
        this.length = length;
        this.offset = offset;
        this.textSentimentClass = textSentimentClass;
    }

    /**
     * Get the length of the text by Unicode standard.
     *
     * @return the length of the text by Unicode standard
     */
    public int getLength() {
        return length;
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
     * Get the score of neutral sentiment.
     *
     * @return the score of neutral sentiment
     */
    public double getNeutralScore() {
        return neutralScore;
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
     * Get the offset of the text sentiment.
     *
     * @return the offset of text sentiment
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the text sentiment enum class: POSITIVE, NEGATIVE, NEUTRAL, MIXED.
     *
     * @return the TextSentimentClass
     */
    public TextSentimentClass getTextSentimentClass() {
        return textSentimentClass;
    }
}
