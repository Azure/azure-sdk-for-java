// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.util.IterableStream;

/**
 * A collection model that contains a list of {@link RecognizePiiEntitiesResult} along with model version and
 * batch's statistics.
 */
public class RecognizePiiEntitiesResultCollection extends IterableStream<RecognizePiiEntitiesResult> {
    private final String modelVersion;
    private final TextDocumentBatchStatistics statistics;

    /**
     * Create a {@link RecognizePiiEntitiesResultCollection} model that maintains a list of
     * {@link RecognizePiiEntitiesResult} along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link RecognizePiiEntitiesResult}.
     * @param modelVersion The model version trained in service for the request.
     * @param statistics The batch statistics of response.
     */
    public RecognizePiiEntitiesResultCollection(Iterable<RecognizePiiEntitiesResult> documentResults,
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
