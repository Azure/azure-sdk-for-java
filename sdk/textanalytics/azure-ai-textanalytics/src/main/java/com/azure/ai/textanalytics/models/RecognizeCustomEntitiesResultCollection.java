// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 *
 */
@Immutable
public final class RecognizeCustomEntitiesResultCollection extends IterableStream<RecognizeCustomEntitiesResult> {
    private final String modelVersion;
    private final TextDocumentBatchStatistics statistics;
    /**
     * Create a {@link RecognizeCustomEntitiesResultCollection} model that maintains a list of
     * {@link RecognizeCustomEntitiesResult} along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link RecognizeCustomEntitiesResult}.
     * @param modelVersion The model version trained in service for the request.
     * @param statistics The batch statistics of response.
     */
    public RecognizeCustomEntitiesResultCollection(Iterable<RecognizeCustomEntitiesResult> documentResults,
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
