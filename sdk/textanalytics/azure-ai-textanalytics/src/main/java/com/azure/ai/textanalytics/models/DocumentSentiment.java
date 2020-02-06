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
    private final SentimentLabel sentimentLabel;
    private final SentimentScorePerLabel sentimentScorePerLabel;
    private final List<SentenceSentiment> sentenceSentiments;

    /**
     * Creates a {@code DocumentSentiment} model that describes the sentiment of the document.
     *
     * @param sentimentLabel the sentiment label of the document
     * @param sentimentScorePerLabel the score of sentiment label of the document
     * @param sentenceSentiments a list of sentence sentiments
     */
    public DocumentSentiment(SentimentLabel sentimentLabel, SentimentScorePerLabel sentimentScorePerLabel,
        List<SentenceSentiment> sentenceSentiments) {
        this.sentimentLabel = sentimentLabel;
        this.sentimentScorePerLabel = sentimentScorePerLabel;
        this.sentenceSentiments = sentenceSentiments;
    }

    /**
     * Get the sentiment label.
     *
     * @return the SentimentLabel
     */
    public SentimentLabel getSentimentLabel() {
        return sentimentLabel;
    }

    /**
     * Get the confidence scores of the sentiment label.
     *
     * @return the SentimentScorePerLabel
     */
    public SentimentScorePerLabel getSentimentScorePerLabel() {
        return sentimentScorePerLabel;
    }

    /**
     * Get a list of sentence sentiments.
     *
     * @return a list of sentence sentiments
     */
    public List<SentenceSentiment> getSentenceSentiments() {
        return sentenceSentiments;
    }
}
