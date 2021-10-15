// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ExtractSummaryResultPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link ExtractSummaryResult} model.
 */
@Immutable
public final class ExtractSummaryResult extends TextAnalyticsResult {
    private SummarySentenceCollection sentences;

    static {
        ExtractSummaryResultPropertiesHelper.setAccessor(
            ((extractSummaryResult, sentences) -> extractSummaryResult.setSentences(sentences))
        );
    }

    /**
     * Creates a {@link ExtractSummaryResult} model that describes extractive summarization result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ExtractSummaryResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Get the extractive summarization sentence collection.
     *
     * @return The extractive summarization sentence collection.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public SummarySentenceCollection getSentences() {
        throwExceptionIfError();
        return sentences;
    }

    private void setSentences(SummarySentenceCollection sentences) {
        this.sentences = sentences;
    }
}
