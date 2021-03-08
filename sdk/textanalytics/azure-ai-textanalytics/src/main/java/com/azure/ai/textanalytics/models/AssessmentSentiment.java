// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AssessmentSentimentPropertiesHelper;

/**
 * The {@link AssessmentSentiment} model.
 */
public final class AssessmentSentiment {
    private String text;
    private TextSentiment sentiment;
    private SentimentConfidenceScores confidenceScores;
    private boolean isNegated;
    private int offset;
    private int length;

    static {
        AssessmentSentimentPropertiesHelper.setAccessor(
            new AssessmentSentimentPropertiesHelper.AssessmentSentimentAccessor() {
                @Override
                public void setText(AssessmentSentiment assessmentSentiment, String text) {
                    assessmentSentiment.setText(text);
                }

                @Override
                public void setSentiment(AssessmentSentiment assessmentSentiment, TextSentiment sentiment) {
                    assessmentSentiment.setSentiment(sentiment);
                }

                @Override
                public void setConfidenceScores(AssessmentSentiment assessmentSentiment,
                    SentimentConfidenceScores confidenceScores) {
                    assessmentSentiment.setConfidenceScores(confidenceScores);
                }

                @Override
                public void setNegated(AssessmentSentiment assessmentSentiment, boolean isNegated) {
                    assessmentSentiment.setNegated(isNegated);
                }

                @Override
                public void setOffset(AssessmentSentiment assessmentSentiment, int offset) {
                    assessmentSentiment.setOffset(offset);
                }

                @Override
                public void setLength(AssessmentSentiment assessmentSentiment, int length) {
                    assessmentSentiment.setLength(length);
                }
            });
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
     * {@code neutral} sentiment type additionally, but target sentiment can only be positive, negative, or mixed.
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

    private void setText(String text) {
        this.text = text;
    }

    private void setSentiment(TextSentiment sentiment) {
        this.sentiment = sentiment;
    }

    private void setConfidenceScores(SentimentConfidenceScores confidenceScores) {
        this.confidenceScores = confidenceScores;
    }

    private void setNegated(boolean isNegated) {
        this.isNegated = isNegated;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
