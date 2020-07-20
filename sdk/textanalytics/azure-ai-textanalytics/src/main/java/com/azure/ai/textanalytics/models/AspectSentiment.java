// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link AspectSentiment} model.
 */
@Immutable
public final class AspectSentiment {
    private final String text;
    private final TextSentiment sentiment;
    private final IterableStream<OpinionSentiment> opinions;
    private final SentimentConfidenceScores confidenceScores;
    private final int length;
    private final int offset;

    /**
     * Create an {@link AspectSentiment} model that describes aspect.
     *
     * @param text The aspect text property.
     * @param sentiment The text sentiment label: POSITIVE, NEGATIVE.
     * @param opinions The opinions of the aspect text.
     * @param offset The offset of aspect text.
     * @param length The length of aspect text.
     * @param confidenceScores The {@link SentimentConfidenceScores}.
     */
    public AspectSentiment(String text, TextSentiment sentiment, IterableStream<OpinionSentiment> opinions,
        int offset, int length, SentimentConfidenceScores confidenceScores) {
        this.text = text;
        this.sentiment = sentiment;
        this.opinions = opinions;
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
     * Get the aspect text sentiment label: POSITIVE, NEGATIVE.
     * @return The sentiment value.
     */
    public TextSentiment getSentiment() {
        return sentiment;
    }

    /**
     * Get the opinions of the aspect text.
     *
     * @return The opinions of the aspect text.
     */
    public IterableStream<OpinionSentiment> getOpinions() {
        return opinions;
    }

    /**
     * Get the offset of aspect text.
     *
     * @return The offset of aspect text.
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
     * Get the confidence score of the sentiment label. All score values sum up to 1, higher the score value means
     * higher confidence the sentiment label represents.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }
}
