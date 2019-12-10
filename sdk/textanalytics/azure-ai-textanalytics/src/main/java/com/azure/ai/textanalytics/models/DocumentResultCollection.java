// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.IterableStream;

/**
 *
 *
 * @param <T>
 */
public class DocumentResultCollection<T> extends IterableStream<T> {

    private String modelVersion;
    private TextBatchStatistics statistics;

    /**
     * Create a document result collection
     *
     * @param iterable a generic iterable that takes type T
     * @param modelVersion model version
     * @param statistics Text batch statistics
     */
    public DocumentResultCollection(Iterable<T> iterable, String modelVersion, TextBatchStatistics statistics) {
        super(iterable);
        this.modelVersion = modelVersion;
        this.statistics = statistics;
    }

    /**
     * Get model version
     *
     * @return model version
     */
    public String getModelVersion() {
        return modelVersion;
    }

    DocumentResultCollection<T> setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     * Get statistics of the batch documents
     *
     * @return the statistics of the batch documents
     */
    public TextBatchStatistics getStatistics() {
        return statistics;
    }

    DocumentResultCollection<T> setBatchStatistics(TextBatchStatistics statistics) {
        this.statistics = statistics;
        return this;
    }
}
