// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.ExtractSummaryResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link ExtractSummaryResult} model.
 */
@Immutable
public final class ExtractSummaryResult extends TextAnalyticsResult {
    private IterableStream<SummarySentence> sentences;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        ExtractSummaryResultPropertiesHelper.setAccessor(
            new ExtractSummaryResultPropertiesHelper.ExtractSummaryResultAccessor() {
                @Override
                public void setSentences(ExtractSummaryResult documentResult,
                    IterableStream<SummarySentence> sentences) {
                    documentResult.setSentences(sentences);
                }

                @Override
                public void setWarnings(ExtractSummaryResult documentResult,
                    IterableStream<TextAnalyticsWarning> warnings) {
                    documentResult.setWarnings(warnings);
                }
            });
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
    public IterableStream<SummarySentence> getSentences() {
        throwExceptionIfError();
        return sentences;
    }

    /**
     * Get the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setSentences(IterableStream<SummarySentence> sentences) {
        this.sentences = sentences;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }
}
