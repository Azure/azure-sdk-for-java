// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link OpinionSentiment} model.
 */
@Immutable
public final class OpinionSentiment {
    private final String text;
    private final TextSentiment sentiment;
    private final SentimentConfidenceScores confidenceScores;
    private final boolean isNegated;
    private final int length;
    private final int offset;

    /**
     * Create an {@link OpinionSentiment} model that describes opinion sentiment.
     *
     * @param text The opinion text property.
     * @param sentiment The text sentiment label: POSITIVE, NEGATIVE, MIXED. {@link TextSentiment} has
     * {@code neutral} sentiment type additionally, but opinion sentiment can only be positive, negative, or mixed.
     * @param offset The offset of opinion text.
     * @param length The length of opinion text.
     * @param isNegated The boolean indicator to show if the opinion text negated or not.
     * @param confidenceScores The {@link SentimentConfidenceScores}.
     */
    public OpinionSentiment(String text, TextSentiment sentiment, int offset, int length,
        boolean isNegated, SentimentConfidenceScores confidenceScores) {
        this.text = text;
        this.sentiment = sentiment;
        this.offset = offset;
        this.length = length;
        this.isNegated = isNegated;
        this.confidenceScores = confidenceScores;
    }

    /**
     * Get the opinion text property.
     *
     * @return The text value.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the opinion text sentiment label: POSITIVE, NEGATIVE, MIXED. {@link TextSentiment} has
     * {@code neutral} sentiment type additionally, but aspect sentiment can only be positive, negative, or mixed.
     *
     * @return The sentiment value.
     */
    public TextSentiment getSentiment() {
        return sentiment;
    }

    /**
     * Get the offset of opinion text.
     *
     * @return The offset of opinion text.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the length of opinion text.
     *
     * @return The length of opinion text.
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the boolean indicator to show if the text is negative.
     *
     * @return The boolean indicator to show if the text is negative.
     */
    public boolean isNegated() {
        return isNegated;
    }

    /**
     * Get the confidence score of the sentiment label. All score values sum up to 1, the higher the score, the
     * higher the confidence in the sentiment. OpinionSentiment only has positive or negative confidence score value
     * because there is no neutral sentiment label in the OpinionSentiment.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }
}
