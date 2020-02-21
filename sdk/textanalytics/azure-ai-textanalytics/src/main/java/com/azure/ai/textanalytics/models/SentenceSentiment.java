// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link SentenceSentiment} model that contains a sentiment label of a sentence, confidence score of the sentiment
 * label, length of the sentence and offset of the sentence within a document.
 */
@Immutable
public final class SentenceSentiment {
    private final int length;
    private final int offset;
    private final SentimentConfidenceScorePerLabel confidenceScores;
    private final SentenceSentimentLabel sentiment;

    /**
     * Creates a {@link SentenceSentiment} model that describes the sentiment analysis of sentence.
     *
     * @param sentiment sentiment label of the sentence.
     * @param confidenceScores the sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     *   Higher values signify higher confidence.
     * @param length length of the sentence.
     * @param offset the offset from the start of the sentence.
     */
    public SentenceSentiment(SentenceSentimentLabel sentiment, SentimentConfidenceScorePerLabel confidenceScores,
        int length, int offset) {
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
        this.length = length;
        this.offset = offset;
    }

    /**
     * Get the length of the sentence by Unicode standard.
     *
     * @return the length of the sentence by Unicode standard.
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the offset of the sentence sentiment.
     *
     * @return the offset of sentence sentiment.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the text sentiment label: POSITIVE, NEGATIVE, or NEUTRAL.
     *
     * @return the {@link SentenceSentimentLabel}.
     */
    public SentenceSentimentLabel getSentiment() {
        return sentiment;
    }

    /**
     * Get the confidence score of the sentiment label. All score values sum up to 1, higher the score value means
     * higher confidence the sentiment label represents.
     *
     * @return the {@link SentimentConfidenceScorePerLabel}.
     */
    public SentimentConfidenceScorePerLabel getConfidenceScores() {
        return confidenceScores;
    }
}
