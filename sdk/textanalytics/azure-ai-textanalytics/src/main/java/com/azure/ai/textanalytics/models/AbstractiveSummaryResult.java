// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link AbstractiveSummaryResult} model.
 */
@Immutable
public final class AbstractiveSummaryResult extends TextAnalyticsResult {
    private final IterableStream<AbstractiveSummary> summaries;

    /**
     * Creates a {@link AbstractiveSummaryResult} model that describes analyzed sentiment result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param summaries The document sentiment.
     */
    public AbstractiveSummaryResult(String id, TextDocumentStatistics textDocumentStatistics,
                                  TextAnalyticsError error, IterableStream<AbstractiveSummary> summaries) {
        super(id, textDocumentStatistics, error);
        this.summaries = summaries;
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
}
