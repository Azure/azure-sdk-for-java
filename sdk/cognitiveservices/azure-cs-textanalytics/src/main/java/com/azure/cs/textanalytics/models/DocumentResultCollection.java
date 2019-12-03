// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.util.IterableStream;
import reactor.core.publisher.Flux;

public class DocumentResultCollection<T> extends IterableStream<T> {

    private String modelVersion;
    private TextBatchStatistics statistics;

    public DocumentResultCollection(Iterable<T> iterable, String modelVersion, TextBatchStatistics statistics) {
        super(iterable);
        this.modelVersion = modelVersion;
        this.statistics = statistics;
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
