// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The DocumentResult model.
 */
@Fluent
public class DocumentResult {
    private String id;
    private TextDocumentStatistics textDocumentStatistics;
    private Error error;
    private boolean isError;

    DocumentResult(final String id, final Error error, final boolean isError) {
        this.id = id;
        this.error = error;
        this.isError = isError;
    }

    DocumentResult(final String id, final TextDocumentStatistics textDocumentStatistics) {
        this.id = id;
        this.textDocumentStatistics = textDocumentStatistics;
    }

    public String getId() {
        return id;
    }

    public TextDocumentStatistics getStatistics() {
        return textDocumentStatistics;
    }

    public Error getError() {
        return error;
    }

    public boolean isError() {
        return isError;
    }
}
