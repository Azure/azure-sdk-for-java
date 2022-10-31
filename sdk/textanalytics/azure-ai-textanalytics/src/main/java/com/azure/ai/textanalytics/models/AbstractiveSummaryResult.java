// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.AbstractiveSummaryResultPropertiesHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link AbstractiveSummaryResult} model.
 */
@Immutable
public final class AbstractiveSummaryResult extends TextAnalyticsResult {
    private IterableStream<AbstractiveSummary> summaries;
    private IterableStream<TextAnalyticsWarning> warnings;

    static {
        AbstractiveSummaryResultPropertiesHelper.setAccessor(
                new AbstractiveSummaryResultPropertiesHelper.AbstractiveSummaryResultAccessor() {
                    @Override
                    public void setWarnings(AbstractiveSummaryResult summaryResult,
                        IterableStream<TextAnalyticsWarning> warnings) {
                        summaryResult.setWarnings(warnings);
                    }

                    @Override
                    public void setSummaries(AbstractiveSummaryResult summaryResult,
                        IterableStream<AbstractiveSummary> summaries) {
                        summaryResult.setSummaries(summaries);
                    }
                });
    }

    /**
     * Creates a {@link AbstractiveSummaryResult} model that describes analyzed sentiment result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public AbstractiveSummaryResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Gets the document sentiment.
     *
     * @return The document sentiment.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public IterableStream<AbstractiveSummary> getSummaries() {
        throwExceptionIfError();
        return summaries;
    }

    /**
     * Gets the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

    private void setWarnings(IterableStream<TextAnalyticsWarning> warnings) {
        this.warnings = warnings;
    }

    private void setSummaries(IterableStream<AbstractiveSummary> summaries) {
        this.summaries = summaries;
    }
}
