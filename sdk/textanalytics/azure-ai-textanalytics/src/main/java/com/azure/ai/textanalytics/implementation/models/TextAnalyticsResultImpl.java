// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.implementation.models;

import com.azure.ai.textanalytics.models.TextAnalyticsResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextAnalyticsException;
import com.azure.ai.textanalytics.models.TextAnalyticsWarning;
import com.azure.ai.textanalytics.models.TextDocumentStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.Locale;

/**
 * The {@link TextAnalyticsResultImpl} model.
 */
@Immutable
public class TextAnalyticsResultImpl implements TextAnalyticsResult {
    private final ClientLogger logger = new ClientLogger(TextAnalyticsResultImpl.class);

    private final String id;
    private final TextDocumentStatistics textDocumentStatistics;
    private final TextAnalyticsError error;
    private final IterableStream<TextAnalyticsWarning> warnings;
    private final boolean isError;

    /**
     * Create a {@link TextAnalyticsResultImpl} model that maintains document id, information about the document
     * payload, and document error.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     *
     */
    TextAnalyticsResultImpl(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error,
                            IterableStream<TextAnalyticsWarning> warnings) {
        this.id = id;
        this.error = error;
        this.isError = error != null;
        this.textDocumentStatistics = textDocumentStatistics;
        this.warnings = warnings;
    }

    /**
     * Get the document id.
     *
     * @return The document id.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the statistics of the text document.
     *
     * @return The {@link TextDocumentStatistics} statistics of the text document.
     */
    public TextDocumentStatistics getStatistics() {
        throwExceptionIfError();
        return textDocumentStatistics;
    }

    /**
     * Get the error of text document.
     *
     * @return The error of text document.
     */
    public TextAnalyticsError getError() {
        return error;
    }

    /**
     * Get the boolean value indicates if the document result is error or not.
     *
     * @return A boolean indicates if the document result is error or not.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Get a {@link IterableStream} of {@link com.azure.ai.textanalytics.models.TextAnalyticsWarning}.
     *
     * @return a {@link IterableStream} of {@link com.azure.ai.textanalytics.models.TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return warnings;
    }

    /**
     * Throw a {@link TextAnalyticsException} if result has isError true and when a non-error property was accessed.
     */
    void throwExceptionIfError() {
        if (this.isError()) {
            throw logger.logExceptionAsError(new TextAnalyticsException(
                String.format(Locale.ROOT,
                    "Error in accessing the property on document id: %s, when %s returned with an error: %s",
                    this.id, this.getClass().getSimpleName(), this.error.getMessage()),
                this.error.getCode().toString(), null));
        }
    }
}
