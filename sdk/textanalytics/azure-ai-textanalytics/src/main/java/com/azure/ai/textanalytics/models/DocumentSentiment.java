// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The DocumentSentiment model
 */
@Immutable
public final class DocumentSentiment {
    private final SentimentLabel sentiment;
    private final SentimentScorePerLabel sentimentScores;
    private final List<SentenceSentiment> sentences;

    /**
     * Creates a {@code DocumentSentiment} model that describes the sentiment of the document.
     *
     * @param sentiment the sentiment label of the document
     * @param sentimentScores the score of sentiment label of the document
     * @param sentences a list of sentence sentiments
     */
    public DocumentSentiment(SentimentLabel sentiment, SentimentScorePerLabel sentimentScores,
        List<SentenceSentiment> sentences) {
        this.sentiment = sentiment;
        this.sentimentScores = sentimentScores;
        this.sentences = sentences;
    }

    /**
     * Get the sentiment label.
     *
     * @return the SentimentLabel
     */
    public SentimentLabel getSentiment() {
        return sentiment;
    }

    /**
     * Get the confidence scores of the sentiment label.
     *
     * @return the SentimentScorePerLabel
     */
    public SentimentScorePerLabel getSentimentScores() {
        return sentimentScores;
    }

    /**
     * Get a list of sentence sentiments.
     *
     * @return a list of sentence sentiments
     */
    public List<SentenceSentiment> getSentences() {
        return sentences;
    }
}
