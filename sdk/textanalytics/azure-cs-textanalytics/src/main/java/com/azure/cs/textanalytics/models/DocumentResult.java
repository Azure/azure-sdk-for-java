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
    private Error error;

    public boolean isError() {
        return isError;
    }

    public void setIsError(final boolean isError) {
        this.isError = isError;
    }

    private boolean isError;

    protected DocumentResult(final String id, final Error error, final boolean isError) {
        this.id = id;
        this.error = error;
        this.isError = isError;
    }

    public DocumentResult(final String id, final TextDocumentStatistics textDocumentStatistics) {
        this.id = id;
        this.textDocumentStatistics = textDocumentStatistics;
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

    public Error getError() {
        return error;
    }

    DocumentResult setError(Error error) {
        this.error = error;
        return this;
    }

}
