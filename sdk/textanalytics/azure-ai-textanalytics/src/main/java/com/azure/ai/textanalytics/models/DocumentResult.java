// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The DocumentResult model.
 */
@Immutable
public class DocumentResult {
    private final String id;
    private final TextDocumentStatistics textDocumentStatistics;
    private final Error error;
    private final boolean isError;

    DocumentResult(final String id, final Error error, final boolean isError) {
        this.id = id;
        this.error = error;
        this.isError = isError;
        this.textDocumentStatistics = null;
    }

    DocumentResult(final String id, final TextDocumentStatistics textDocumentStatistics) {
        this.id = id;
        this.error = null;
        this.isError = false;
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
