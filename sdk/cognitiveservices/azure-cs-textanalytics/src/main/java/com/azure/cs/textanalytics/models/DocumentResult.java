// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import java.util.List;

public class DocumentResult<T> {

    private String id;
    private TextDocumentStatistics textDocumentStatistics;
    private List<T> items;

    public String getId() {
        return id;
    }

    public DocumentResult setId(String id) {
        this.id = id;
        return this;
    }

    public TextDocumentStatistics getTextDocumentStatistics() {
        return textDocumentStatistics;
    }

    public DocumentResult setTextDocumentStatistics(TextDocumentStatistics textDocumentStatistics) {
        this.textDocumentStatistics = textDocumentStatistics;
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
