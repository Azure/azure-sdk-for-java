// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The DocumentResult model.
 */
@Fluent
public class DocumentResult {

    private String id;
    private TextDocumentStatistics textDocumentStatistics;
    private Object error;

    public DocumentResult(final String id, final TextDocumentStatistics textDocumentStatistics, final Object error) {
        this.id = id;
        this.textDocumentStatistics = textDocumentStatistics;
        this.error = error;
    }

    public String getId() {
        return id;
    }

    DocumentResult setId(String id) {
        this.id = id;
        return this;
    }

    public TextDocumentStatistics getStatistics() {
        return textDocumentStatistics;
    }

    DocumentResult setTextDocumentStatistics(TextDocumentStatistics textDocumentStatistics) {
        this.textDocumentStatistics = textDocumentStatistics;
        return this;
    }

    public Object getError() {
        return error;
    }

    DocumentResult setError(Object error) {
        this.error = error;
        return this;
    }

}
