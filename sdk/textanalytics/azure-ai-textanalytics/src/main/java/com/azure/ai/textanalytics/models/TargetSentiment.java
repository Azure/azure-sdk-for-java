// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.TargetSentimentPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link TargetSentiment} model.
 */
@Immutable
public final class TargetSentiment {
    private String text;
    private TextSentiment sentiment;
    private SentimentConfidenceScores confidenceScores;
    private int offset;
    private int length;

    static {
        TargetSentimentPropertiesHelper.setAccessor(new TargetSentimentPropertiesHelper.TargetSentimentAccessor() {
            @Override
            public void setText(TargetSentiment targetSentiment, String text) {
                targetSentiment.setText(text);
            }

            @Override
            public void setSentiment(TargetSentiment targetSentiment, TextSentiment sentiment) {
                targetSentiment.setSentiment(sentiment);
            }

            @Override
            public void setConfidenceScores(TargetSentiment targetSentiment, SentimentConfidenceScores confidenceScores) {
                targetSentiment.setConfidenceScores(confidenceScores);
            }

            @Override
            public void setOffset(TargetSentiment targetSentiment, int offset) {
                targetSentiment.setOffset(offset);
            }

            @Override
            public void setLength(TargetSentiment targetSentiment, int length) {
                targetSentiment.setLength(length);
            }
        });
    }

    /**
     * Gets the target text property.
     *
     * @return The text value.
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the target text sentiment label: POSITIVE, NEGATIVE, MIXED. {@link TextSentiment} has
     * NEUTRAL sentiment type additionally, but target sentiment can only be POSITIVE, NEGATIVE, or MIXED.
     *
     * @return The sentiment value.
     */
    public TextSentiment getSentiment() {
        return sentiment;
    }

    /**
     * Gets the target text offset from the start of document.
     *
     * @return The target text offset from the start of document.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the length of target text.
     *
     * @return The length of target text.
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the confidence score of the sentiment label. All score values sum up to 1, the higher the score, the
     * higher the confidence in the sentiment. AspectSentiment only has positive or negative confidence score value
     * because there is no neutral sentiment label in the AspectSentiment.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }

    private void setText(String text) {
        this.text = text;
    }

    private void setSentiment(TextSentiment sentiment) {
        this.sentiment = sentiment;
    }

    private void setConfidenceScores(SentimentConfidenceScores confidenceScores) {
        this.confidenceScores = confidenceScores;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
