// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.util.IterableStream;

/**
 * A collection model that contains a list of {@link AnalyzeSentimentResult} along with model version and
 * batch's statistics.
 */
public class AnalyzeSentimentResultCollection extends IterableStream<AnalyzeSentimentResult> {
    private final String modelVersion;
    private final TextDocumentBatchStatistics statistics;
    /**
     * Creates a {@link AnalyzeSentimentResultCollection} model that maintains a list of {@link AnalyzeSentimentResult}
     * along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link AnalyzeSentimentResult}.
     * @param modelVersion The model version trained in service for the request.
     * @param statistics The batch statistics of response.
     */
    public AnalyzeSentimentResultCollection(Iterable<AnalyzeSentimentResult> documentResults, String modelVersion,
        TextDocumentBatchStatistics statistics) {
        super(documentResults);
        this.modelVersion = modelVersion;
        this.statistics = statistics;
    }

    /**
     * Gets the model version trained in service for the request.
     *
     * @return The model version trained in service for the request.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Gets the batch statistics of response.
     *
     * @return The batch statistics of response.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return statistics;
    }
}
