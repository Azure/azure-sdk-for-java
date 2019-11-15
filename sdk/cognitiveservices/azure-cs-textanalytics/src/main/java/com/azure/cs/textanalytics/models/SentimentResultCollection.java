// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import java.util.List;

public class SentimentResultCollection {
    private List<DocumentError> errors;
    private String modelVersion;
    private DocumentStatistics statistics;

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

    public DocumentStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(DocumentStatistics statistics) {
        this.statistics = statistics;
    }
}
