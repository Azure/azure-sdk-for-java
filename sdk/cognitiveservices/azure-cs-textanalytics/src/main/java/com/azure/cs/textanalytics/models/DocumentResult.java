// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import java.util.List;

public class DocumentResult<T> {

    private String id;
    private DocumentStatistics documentStatistics;
    private List<DocumentError> errors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentStatistics getDocumentStatistics() {
        return documentStatistics;
    }

    public void setDocumentStatistics(DocumentStatistics documentStatistics) {
        this.documentStatistics = documentStatistics;
    }
}
