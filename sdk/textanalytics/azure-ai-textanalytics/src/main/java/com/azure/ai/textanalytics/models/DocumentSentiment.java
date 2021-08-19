// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link DocumentSentiment} model that contains sentiment label of a document, confidence score of the
 * sentiment label, and a list of {@link SentenceSentiment}.
 */
@Immutable
public final class DocumentSentiment {
    private final TextSentiment sentiment;
    private final SentimentConfidenceScores confidenceScores;
    private final IterableStream<SentenceSentiment> sentences;
    private final IterableStream<TextAnalyticsWarning> warnings;

    /**
     * Creates a {@link DocumentSentiment} model that describes the sentiment of the document.
     *
     * @param sentiment The sentiment label of the document.
     * @param confidenceScores The sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     *   Higher values signify higher confidence.
     * @param sentences An {@link IterableStream} of sentence sentiments.
     * @param warnings An {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public DocumentSentiment(TextSentiment sentiment, SentimentConfidenceScores confidenceScores,
        IterableStream<SentenceSentiment> sentences, IterableStream<TextAnalyticsWarning> warnings) {
        this.sentiment = sentiment;
        this.confidenceScores = confidenceScores;
        this.sentences = sentences;
        this.warnings = warnings;
    }

    /**
     * Gets the text sentiment label: POSITIVE, NEGATIVE, NEUTRAL, or MIXED.
     *
     * @return The {@link TextSentiment}.
     */
    public TextSentiment getSentiment() {
        return sentiment;
    }

    /**
     * Gets the sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     * Higher values signify higher confidence.
     *
     * @return The {@link SentimentConfidenceScores}.
     */
    public SentimentConfidenceScores getConfidenceScores() {
        return confidenceScores;
    }

    /**
     * Gets a list of sentence sentiments.
     *
     * @return A list of sentence sentiments.
     */
    public IterableStream<SentenceSentiment> getSentences() {
        return sentences;
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return An {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }
}
