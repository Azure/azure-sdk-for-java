// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.util.IterableStream;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class DocumentResultCollection<T> extends IterableStream<DocumentResult<T>> {

    private List<DocumentError> errors;
    private String modelVersion;
    private DocumentBatchStatistics statistics;

    /**
     * Creates instance with the given {@link Flux}.
     *
     * @param flux Flux of items to iterate over.
     */
    public DocumentResultCollection(Flux<DocumentResult<T>> flux) {
        super(flux);
    }

    public List<DocumentError> getErrors() {
        return errors;
    }

    public void setErrors(List<DocumentError> errors) {
        this.errors = errors;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public DocumentBatchStatistics getBatchStatistics() {
        return statistics;
    }

    public void setBatchStatistics(DocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }
}
