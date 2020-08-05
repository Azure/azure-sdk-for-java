// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link SentenceSentiment} model that contains a sentiment label of a sentence, confidence score of the
 * sentiment label, length of the sentence and offset of the sentence within a document.
 */
@Immutable
public final class SentenceSentiment {
    private final String text;
    private final SentimentConfidenceScores confidenceScores;
    private final TextSentiment sentiment;
    private final IterableStream<MinedOpinion> minedOpinions;

    /**
     * Creates a {@link SentenceSentiment} model that describes the sentiment analysis of sentence.
     * @param text The sentence text.
     * @param sentiment The sentiment label of the sentence.
     * @param confidenceScores The sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     *   Higher values signify higher confidence.
     */
    public SentenceSentiment(String text, TextSentiment sentiment, SentimentConfidenceScores confidenceScores) {
        this.text = text;
        this.sentiment = sentiment;
        this.minedOpinions = null;
        this.confidenceScores = confidenceScores;
    }

    /**
     * Creates a {@link SentenceSentiment} model that describes the sentiment analysis of sentence.
     * @param text The sentence text.
     * @param sentiment The sentiment label of the sentence.
     * @param minedOpinions The mined opinions of the sentence sentiment.
     * @param confidenceScores The sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     *   Higher values signify higher confidence.
     */
    public SentenceSentiment(String text, TextSentiment sentiment, IterableStream<MinedOpinion> minedOpinions,
        SentimentConfidenceScores confidenceScores) {
        this.text = text;
        this.sentiment = sentiment;
        this.minedOpinions = minedOpinions;
        this.confidenceScores = confidenceScores;
    }

    /**
     * Get the sentence text property.
     *
     * @return the text property value.
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
     * Get the mined opinions of sentence sentiment.
     *
     * @return The mined opinions of sentence sentiment.
     */
    public IterableStream<MinedOpinion> getMinedOpinions() {
        return minedOpinions;
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
