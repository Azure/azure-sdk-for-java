// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import java.util.ArrayList;
import java.util.List;

public class DocumentResultCollection<T> extends ArrayList<DocumentResult<T>> {
    private List<DocumentError> errors;
    private String modelVersion;
    private DocumentBatchStatistics statistics;

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

    public DocumentBatchStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(DocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }
}
