// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The DocumentResultCollection model.
 *
 * @param <T> the type of DocumentResultCollection holds
 */
@Immutable
public final class DocumentResultCollection<T> extends IterableStream<T> {

    private final String modelVersion;
    private final TextDocumentBatchStatistics statistics;

    /**
     * Create a document result collection.
     *
     * @param iterable a generic iterable that takes type T
     * @param modelVersion model version
     * @param statistics Text batch statistics
     */
    public DocumentResultCollection(Iterable<T> iterable, String modelVersion, TextDocumentBatchStatistics statistics) {
        super(iterable);
        this.modelVersion = modelVersion;
        this.statistics = statistics;
    }

    /**
     * Get model version.
     *
     * @return model version
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Get statistics of the batch documents.
     *
     * @return the statistics of the batch documents
     */
    public TextDocumentBatchStatistics getStatistics() {
        return statistics;
    }
}
