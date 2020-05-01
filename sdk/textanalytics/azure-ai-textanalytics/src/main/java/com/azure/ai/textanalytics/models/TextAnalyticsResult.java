// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.util.IterableStream;

/**
 * The {@link TextAnalyticsResult} model.
 */
public interface TextAnalyticsResult {
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

    /**
     * Get a {@link IterableStream} of {@link TextAnalyticsWarning}.
     *
     * @return a {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    IterableStream<TextAnalyticsWarning> getWarnings();
}
