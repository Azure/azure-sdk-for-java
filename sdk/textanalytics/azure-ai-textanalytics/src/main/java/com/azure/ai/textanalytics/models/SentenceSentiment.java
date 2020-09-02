// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link SentenceSentiment} model that contains a sentiment label of a sentence, confidence scores of the
 * sentiment label, mined opinions, offset of sentence, and length of sentence within a document.
 */
@Immutable
public final class SentenceSentiment {
    private final String text;
    private final TextSentiment sentiment;
    private final SentimentConfidenceScores confidenceScores;
    private final IterableStream<MinedOpinion> minedOpinions;
    private final int offset;
    private final int length;

    /**
     * Creates a {@link SentenceSentiment} model that describes the sentiment analysis of sentence.
     *
     * @param text The sentence text.
     * @param sentiment The sentiment label of the sentence.
     * @param confidenceScores The sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     * Higher values signify higher confidence.
     */
    public SentenceSentiment(String text, TextSentiment sentiment, SentimentConfidenceScores confidenceScores) {
        this.text = text;
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
        this.minedOpinions = null;
        this.offset = 0;
        this.length = 0;
    }

    /**
     * Creates a {@link SentenceSentiment} model that describes the sentiment analysis of sentence.
     *
     * @param text The sentence text.
     * @param sentiment The sentiment label of the sentence.
     * @param confidenceScores The sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     * Higher values signify higher confidence.
     * @param minedOpinions The mined opinions of the sentence sentiment. This is only returned if you pass the
     * opinion mining parameter to the analyze sentiment APIs.
     * @param offset The start position for the sentence in a document.
     * @param length The length of sentence.
     */
    public SentenceSentiment(String text, TextSentiment sentiment, SentimentConfidenceScores confidenceScores,
        IterableStream<MinedOpinion> minedOpinions, int offset, int length) {
        this.text = text;
        this.sentiment = sentiment;
        this.minedOpinions = minedOpinions;
        this.confidenceScores = confidenceScores;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Get the sentence text property.
     *
     * @return The text property value.
     */
    public String getText() {
        return this.text;
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
     * Get the confidence score of the sentiment label. All score values sum up to 1, the higher the score, the
     * higher the confidence in the sentiment.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }

    /**
     * Get the mined opinions of sentence sentiment.
     * This is only returned if you pass the opinion mining parameter to the analyze sentiment APIs.
     *
     * @return The mined opinions of sentence sentiment.
     */
    public IterableStream<MinedOpinion> getMinedOpinions() {
        return minedOpinions;
    }

    /**
     * Get the offset of sentence. The start position for the sentence in a document.
     *
     * @return The offset of sentence.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the length of sentence.
     *
     * @return The length of sentence.
     */
    public int getLength() {
        return length;
    }
}
