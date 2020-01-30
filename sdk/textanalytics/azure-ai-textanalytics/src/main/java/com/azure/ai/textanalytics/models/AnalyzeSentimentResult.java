// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * The AnalyzeSentimentResult model.
 */
@Immutable
public final class AnalyzeSentimentResult extends DocumentResult {
    private final TextSentiment documentSentiment;
    private final List<TextSentiment> sentenceSentiments;
    private final ClientLogger logger = new ClientLogger(AnalyzeSentimentResult.class);

    /**
     * Creates a {@code TextSentimentResult} model that describes analyzed sentiment result
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error
     * @param documentSentiment the document sentiment
     * @param sentenceSentiments a list of sentence sentiments
     */
    public AnalyzeSentimentResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error,
        TextSentiment documentSentiment, List<TextSentiment> sentenceSentiments) {
        super(id, textDocumentStatistics, error);
        this.documentSentiment = documentSentiment;
        this.sentenceSentiments = sentenceSentiments == null ? new ArrayList<>() : sentenceSentiments;
    }

    /**
     * Get the document sentiment.
     *
     * @return the document sentiment
     */
    public TextSentiment getDocumentSentiment() {
        throwExceptionIfError();
        return documentSentiment;
    }

    /**
     * Get a list of sentence sentiments.
     *
     * @return a list of sentence sentiments
     */
    public List<TextSentiment> getSentenceSentiments() {
        throwExceptionIfError();
        return sentenceSentiments;
    }
}
