// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

/**
 * The {@link DocumentResult} model.
 */
public interface DocumentResult {
    /**
     * Get the document id.
     *
     * @return The document id.
     */
    String getId();

    /**
     * Get the statistics of the text document.
     *
     * @return The {@link TextDocumentStatistics} statistics of the text document.
     */
    TextDocumentStatistics getStatistics();

    /**
     * Get the error of text document.
     *
     * @return The error of text document.
     */
    TextAnalyticsError getError();

    /**
     * Get the boolean value indicates if the document result is error or not.
     *
     * @return A boolean indicates if the document result is error or not.
     */
    boolean isError();
}
