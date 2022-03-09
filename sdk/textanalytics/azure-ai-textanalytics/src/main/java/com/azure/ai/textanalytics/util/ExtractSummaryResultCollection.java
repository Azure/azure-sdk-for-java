// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.models.ExtractSummaryResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * A collection model that contains a list of {@link ExtractSummaryResult} along with model version and
 * batch's statistics.
 */
@Immutable
public final class ExtractSummaryResultCollection extends IterableStream<ExtractSummaryResult> {
    private final String modelVersion;
    private final TextDocumentBatchStatistics statistics;

    /**
     * Create a {@link ExtractSummaryResultCollection} model that maintains a list of
     * {@link ExtractSummaryResult} along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link ExtractSummaryResult}.
     * @param modelVersion The model version trained in service for the request.
     * @param statistics The batch statistics of response.
     */
    public ExtractSummaryResultCollection(Iterable<ExtractSummaryResult> documentResults,
        String modelVersion, TextDocumentBatchStatistics statistics) {
        super(documentResults);
        this.modelVersion = modelVersion;
        this.statistics = statistics;
    }

    /**
     * Get the model version trained in service for the request.
     *
     * @return The model version trained in service for the request.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Get the batch statistics of response.
     *
     * @return The batch statistics of response.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return statistics;
    }
}
