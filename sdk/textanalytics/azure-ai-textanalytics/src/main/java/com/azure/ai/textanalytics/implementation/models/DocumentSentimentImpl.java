// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.SentenceSentiment;
import com.azure.ai.textanalytics.models.SentimentConfidenceScores;
import com.azure.ai.textanalytics.models.TextSentiment;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link DocumentSentimentImpl} model that contains sentiment label of a document, confidence score of the
 * sentiment label, and a list of {@link com.azure.ai.textanalytics.models.SentenceSentiment}.
 */
@Immutable
public final class DocumentSentimentImpl implements DocumentSentiment {
    private final TextSentiment sentiment;
    private final SentimentConfidenceScores confidenceScores;
    private final IterableStream<com.azure.ai.textanalytics.models.SentenceSentiment> sentences;

    /**
     * Creates a {@link DocumentSentimentImpl} model that describes the sentiment of the document.
     *
     * @param sentiment the sentiment label of the document.
     * @param confidenceScores the sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     *   Higher values signify higher confidence.
     * @param sentences a list of sentence sentiments.
     */
    public DocumentSentimentImpl(TextSentiment sentiment, SentimentConfidenceScores confidenceScores,
                                 IterableStream<com.azure.ai.textanalytics.models.SentenceSentiment> sentences) {
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
        this.sentences = sentences;
    }

    /**
     * Get the text sentiment label: POSITIVE, NEGATIVE, NEUTRAL, or MIXED.
     *
     * @return the {@link TextSentiment}.
     */
    public TextSentiment getSentiment() {
        return sentiment;
    }

    /**
     * Get the sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     * Higher values signify higher confidence.
     *
     * @return the {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }

    /**
     * Get a list of sentence sentiments.
     *
     * @return a list of sentence sentiments.
     */
    public IterableStream<SentenceSentiment> getSentences() {
        return sentences;
    }
}
