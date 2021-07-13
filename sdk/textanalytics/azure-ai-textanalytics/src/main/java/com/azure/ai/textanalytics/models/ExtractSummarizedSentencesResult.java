// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class ExtractSummarizedSentencesResult extends TextAnalyticsResult {
    private final SummarizedSentencesCollection sentences;

    /**
     * Creates a {@link AnalyzeSentimentResult} model that describes analyzed sentiment result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param sentences The summarized sentences.
     */
    public ExtractSummarizedSentencesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, SummarizedSentencesCollection sentences) {
        super(id, textDocumentStatistics, error);
        this.sentences = sentences;
    }

    /**
     * Get the summarized sentences.
     *
     * @return The summarized sentences.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public SummarizedSentencesCollection getSentences() {
        throwExceptionIfError();
        return sentences;
    }
}
