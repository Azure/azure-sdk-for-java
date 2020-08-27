// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link AspectSentiment} model.
 */
@Immutable
public final class AspectSentiment {
    private final String text;
    private final TextSentiment sentiment;
    private final SentimentConfidenceScores confidenceScores;
    private final int length;
    private final int offset;

    /**
     * Create an {@link AspectSentiment} model that describes aspect.
     *
     * @param text The aspect text property.
     * @param sentiment The text sentiment label: POSITIVE, NEGATIVE, MIXED. {@link TextSentiment} has
     * NEUTRAL sentiment type additionally, but aspect sentiment can only be POSITIVE, NEGATIVE, or MIXED.
     * @param offset The aspect text offset from the start of document.
     * @param length The length of aspect text.
     * @param confidenceScores The {@link SentimentConfidenceScores}.
     */
    public AspectSentiment(String text, TextSentiment sentiment,
        int offset, int length, SentimentConfidenceScores confidenceScores) {
        this.text = text;
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
        this.length = length;
        this.offset = offset;
    }

    /**
     * Get the aspect text property.
     *
     * @return The text value.
     */
    public String getText() {
        return text;
    }

    /**
     * Get the aspect text sentiment label: POSITIVE, NEGATIVE, MIXED. {@link TextSentiment} has
     * NEUTRAL sentiment type additionally, but aspect sentiment can only be POSITIVE, NEGATIVE, or MIXED.
     *
     * @return The sentiment value.
     */
    public TextSentiment getSentiment() {
        return sentiment;
    }


    /**
     * Get the aspect text offset from the start of document.
     *
     * @return The aspect text offset from the start of document.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the length of aspect text.
     *
     * @return The length of aspect text.
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the confidence score of the sentiment label. All score values sum up to 1, the higher the score, the
     * higher the confidence in the sentiment. AspectSentiment only has positive or negative confidence score value
     * because there is no neutral sentiment label in the AspectSentiment.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }
}
