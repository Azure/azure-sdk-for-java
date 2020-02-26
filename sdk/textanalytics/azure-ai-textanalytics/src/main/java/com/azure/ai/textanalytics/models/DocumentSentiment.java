// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The {@link DocumentSentiment} model that contains sentiment label of a document, confidence score of the sentiment
 * label, and a list of {@link SentenceSentiment}.
 */
@Immutable
public final class DocumentSentiment {
    private final DocumentSentimentLabel sentiment;
    private final SentimentConfidenceScorePerLabel confidenceScores;
    private final List<SentenceSentiment> sentences;

    /**
     * Creates a {@link DocumentSentiment} model that describes the sentiment of the document.
     *
     * @param sentiment the sentiment label of the document.
     * @param confidenceScores the sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     *   Higher values signify higher confidence.
     * @param sentences a list of sentence sentiments.
     */
    public DocumentSentiment(DocumentSentimentLabel sentiment, SentimentConfidenceScorePerLabel confidenceScores,
        List<SentenceSentiment> sentences) {
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
        this.sentences = sentences;
    }

    /**
     * Get the text sentiment label: POSITIVE, NEGATIVE, NEUTRAL, or MIXED.
     *
     * @return the {@link DocumentSentimentLabel}.
     */
    public DocumentSentimentLabel getSentiment() {
        return sentiment;
    }

    /**
     * Get the sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     * Higher values signify higher confidence.
     *
     * @return the {@link SentimentConfidenceScorePerLabel}.
     */
    public SentimentConfidenceScorePerLabel getConfidenceScores() {
        return confidenceScores;
    }

    /**
     * Get a list of sentence sentiments.
     *
     * @return a list of sentence sentiments.
     */
    public List<SentenceSentiment> getSentences() {
        return sentences;
    }
}
