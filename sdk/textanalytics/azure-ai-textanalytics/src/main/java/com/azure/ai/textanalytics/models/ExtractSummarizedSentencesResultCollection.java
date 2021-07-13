// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 *
 */
@Immutable
public final class ExtractSummarizedSentencesResultCollection extends IterableStream<ExtractSummarizedSentencesResult> {

    private final String modelVersion;
    private final TextDocumentBatchStatistics statistics;
    /**
     * Create a {@link ExtractSummarizedSentencesResultCollection} model that maintains a list of
     * {@link ExtractSummarizedSentencesResult} along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link ExtractSummarizedSentencesResult}.
     * @param modelVersion The model version trained in service for the request.
     * @param statistics The batch statistics of response.
     */
    public ExtractSummarizedSentencesResultCollection(Iterable<ExtractSummarizedSentencesResult> documentResults,
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
