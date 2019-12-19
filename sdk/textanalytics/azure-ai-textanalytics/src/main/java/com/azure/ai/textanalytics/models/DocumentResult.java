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
    private final TextAnalyticsError error;
    private final boolean isError;

    /**
     * Create a {@code DocumentResult} model that maintains document id, information about the document payload,
     * and document error
     *
     * @param id unique, non-empty document identifier
     * @param textDocumentStatistics text document statistics
     * @param error the document error.
     */
    DocumentResult(final String id, final TextDocumentStatistics textDocumentStatistics, final TextAnalyticsError error) {
        this.id = id;
        this.error = error;
        this.isError = error != null;
        this.textDocumentStatistics = textDocumentStatistics;
    }

    /**
     * Get the document id
     *
     * @return the document id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the statistics of the text document
     *
     * @return the {@link TextDocumentStatistics} statistics of the text document
     */
    public TextDocumentStatistics getStatistics() {
        return textDocumentStatistics;
    }

    /**
     * Get the error of text document
     *
     * @return the error of text document
     */
    public TextAnalyticsError getError() {
        return error;
    }

    /**
     * Get the boolean value indicates if the document result is error or not
     *
     * @return A boolean indicates if the document result is error or not
     */
    public boolean isError() {
        return isError;
    }
}
