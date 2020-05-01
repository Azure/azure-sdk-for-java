// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link AnalyzeSentimentResultImpl} model.
 */
@Immutable
public final class AnalyzeSentimentResultImpl extends DocumentResultImpl implements AnalyzeSentimentResult {
    private final DocumentSentiment documentSentiment;

    /**
     * Creates a {@link AnalyzeSentimentResultImpl} model that describes analyzed sentiment result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param documentSentiment The document sentiment.
     * @param warnings A {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public AnalyzeSentimentResultImpl(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, DocumentSentiment documentSentiment, IterableStream<TextAnalyticsWarning> warnings) {
        super(id, textDocumentStatistics, error, warnings);
        this.documentSentiment = documentSentiment;
    }

    /**
     * Get the document sentiment.
     *
     * @return The document sentiment.
     */
    public DocumentSentiment getDocumentSentiment() {
        throwExceptionIfError();
        return documentSentiment;
    }
}
