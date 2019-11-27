// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.util.IterableStream;
import reactor.core.publisher.Flux;

public class DocumentResultCollection<T> extends IterableStream<T> {

    private String modelVersion;
    private TextBatchStatistics statistics;

    /**
     * Creates instance with the given {@link Flux}.
     *
     * @param flux Flux of items to iterate over.
     */
    DocumentResultCollection(Flux<T> flux) {
        super(flux);
    }

    public String getModelVersion() {
        return modelVersion;
    }

    DocumentResultCollection<T> setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    public TextBatchStatistics getStatistics() {
        return statistics;
    }

    DocumentResultCollection<T> setBatchStatistics(TextBatchStatistics statistics) {
        this.statistics = statistics;
        return this;
    }
}
