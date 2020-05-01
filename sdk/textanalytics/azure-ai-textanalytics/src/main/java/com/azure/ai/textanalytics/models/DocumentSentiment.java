// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.IterableStream;

/**
 * The {@link DocumentSentiment} model that contains sentiment label of a document, confidence score of the sentiment
 * label, and a list of {@link SentenceSentiment}.
 */
public interface DocumentSentiment {
    /**
     * Get the text sentiment label: POSITIVE, NEGATIVE, NEUTRAL, or MIXED.
     *
     * @return the {@link TextSentiment}.
     */
    TextSentiment getSentiment();

    /**
     * Get the sentiment confidence score (Softmax score) between 0 and 1, for each sentiment label.
     * Higher values signify higher confidence.
     *
     * @return the {@link SentimentConfidenceScores}.
     */
    SentimentConfidenceScores getConfidenceScores();

    /**
     * Get a list of sentence sentiments.
     *
     * @return a list of sentence sentiments.
     */
    IterableStream<SentenceSentiment> getSentences();
}
