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
    private final int graphemeLength;
    private final int graphemeOffset;
    private final SentimentConfidenceScores confidenceScores;
    private final TextSentiment sentiment;

    /**
     * Creates a {@link SentenceSentiment} model that describes the sentiment analysis of sentence.
     *
     * @param sentiment The sentiment label of the sentence.
     * @param confidenceScores The sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     *   Higher values signify higher confidence.
     * @param graphemeLength The grapheme length of the sentence.
     * @param graphemeOffset The grapheme offset, start position for the sentence sentiment.
     */
    public SentenceSentiment(TextSentiment sentiment, SentimentConfidenceScores confidenceScores,
        int graphemeLength, int graphemeOffset) {
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
        this.graphemeLength = graphemeLength;
        this.graphemeOffset = graphemeOffset;
    }

    /**
     * Get the grapheme length of the sentence.
     *
     * @return The grapheme length of the sentence.
     */
    public int getGraphemeLength() {
        return graphemeLength;
    }

    /**
     * Get the grapheme offset property: start position for the sentence sentiment.
     *
     * @return The grapheme offset of sentence sentiment.
     */
    public int getGraphemeOffset() {
        return graphemeOffset;
    }

    /**
     * Get the text sentiment label: POSITIVE, NEGATIVE, or NEUTRAL.
     *
     * @return The {@link TextSentiment}.
     */
    public TextSentiment getSentiment() {
        return sentiment;
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
