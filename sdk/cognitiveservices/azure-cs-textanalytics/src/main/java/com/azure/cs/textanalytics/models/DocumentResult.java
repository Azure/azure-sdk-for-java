// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The DocumentResult model.
 */
@Fluent
public class DocumentResult {

    private String id;
    private TextDocumentStatistics textDocumentStatistics;
    private DocumentError error;

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

    public DocumentError getError() {
        return error;
    }

    public DocumentResult setError(DocumentError error) {
        this.error = error;
        return this;
    }

}
