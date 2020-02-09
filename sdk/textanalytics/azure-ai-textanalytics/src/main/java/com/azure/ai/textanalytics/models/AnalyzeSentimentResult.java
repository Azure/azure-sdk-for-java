// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The AnalyzeSentimentResult model.
 */
@Immutable
public final class AnalyzeSentimentResult extends DocumentResult {
    private final DocumentSentiment documentSentiment;

    /**
     * Creates a {@code TextSentimentResult} model that describes analyzed sentiment result.
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error
     * @param documentSentiment the document sentiment
     */
    public AnalyzeSentimentResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error,
        DocumentSentiment documentSentiment) {
        super(id, textDocumentStatistics, error);
        this.documentSentiment = documentSentiment;
    }

    /**
     * Get the document sentiment.
     *
     * @return the document sentiment
     */
    public DocumentSentiment getDocumentSentiment() {
        throwExceptionIfError();
        return documentSentiment;
    }
}
