// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The TextSentimentResult model.
 */
@Immutable
public final class TextSentimentResult extends DocumentResult {
    private final TextSentiment documentSentiment;
    private final List<TextSentiment> sentenceSentiments;

    /**
     * Creates a {@code TextSentimentResult} model that describes analyzed sentiment result
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error
     * @param documentSentiment the document sentiment
     * @param sentenceSentiments a list of sentence sentiments
     */
    public TextSentimentResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error,
        TextSentiment documentSentiment, List<TextSentiment> sentenceSentiments) {
        super(id, textDocumentStatistics, error);
        this.documentSentiment = documentSentiment;
        this.sentenceSentiments = sentenceSentiments;
    }

    /**
     * Get the document sentiment.
     *
     * @return the document sentiment
     */
    public TextSentiment getDocumentSentiment() {
        return documentSentiment;
    }

    /**
     * Get a list of sentence sentiments.
     *
     * @return a list of sentence sentiments
     */
    public List<TextSentiment> getSentenceSentiments() {
        return sentenceSentiments;
    }


}
