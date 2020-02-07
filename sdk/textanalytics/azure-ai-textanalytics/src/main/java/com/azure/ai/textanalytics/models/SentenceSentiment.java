// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The SentenceSentiment model.
 */
@Immutable
public final class SentenceSentiment {
    private final int length;
    private final int offset;
    private final SentimentScorePerLabel sentimentScores;
    private final SentimentLabel sentiment;

    /**
     * Creates a {@code SentenceSentiment} model that describes the sentiment analysis of sentence.
     *
     * @param sentiment sentiment label of the sentence
     * @param sentimentScores the score of sentiment label of the sentence
     * @param length length of the sentence
     * @param offset the offset from the start of the sentence
     */
    public SentenceSentiment(SentimentLabel sentiment, SentimentScorePerLabel sentimentScores, int length,
                             int offset) {
        this.sentiment = sentiment;
        this.sentimentScores = sentimentScores;
        this.length = length;
        this.offset = offset;
    }

    /**
     * Get the length of the sentence by Unicode standard.
     *
     * @return the length of the sentence by Unicode standard
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the offset of the sentence sentiment.
     *
     * @return the offset of sentence sentiment
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the text sentiment labels: POSITIVE, NEGATIVE, NEUTRAL, MIXED.
     *
     * @return the SentimentLabel
     */
    public SentimentLabel getSentiment() {
        return sentiment;
    }

    /**
     * Get the score of the sentiment label. All score values sum up to 1, higher the score value means higher
     * confidence the sentiment label represents.
     *
     * @return the SentimentScorePerLabel
     */
    public SentimentScorePerLabel getSentimentScores() {
        return sentimentScores;
    }
}
