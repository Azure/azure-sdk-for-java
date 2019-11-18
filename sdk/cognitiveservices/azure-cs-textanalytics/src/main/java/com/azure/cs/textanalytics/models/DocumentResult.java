// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import java.util.List;

public class DocumentResult<T> {

    private String id;
    private DocumentStatistics documentStatistics;
    private List<T> items;

    public String getId() {
        return id;
    }

    public DocumentResult setId(String id) {
        this.id = id;
        return this;
    }

    public DocumentStatistics getDocumentStatistics() {
        return documentStatistics;
    }

    public DocumentResult setDocumentStatistics(DocumentStatistics documentStatistics) {
        this.documentStatistics = documentStatistics;
        return this;
    }

    public List<T> getItems() {
        return items;
    }

    public DocumentResult setItems(List<T> items) {
        this.items = items;
        return this;
    }
}
