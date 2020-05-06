// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link DetectLanguageResult} model.
 */
@Immutable
public final class DetectLanguageResult extends TextAnalyticsResult {
    private final DetectedLanguage primaryLanguage;

    /**
     * Create a {@link DetectLanguageResult} model that describes detected languages result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param primaryLanguage The detected primary language.
     * @param warnings A {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public DetectLanguageResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, DetectedLanguage primaryLanguage, IterableStream<TextAnalyticsWarning> warnings) {
        super(id, textDocumentStatistics, error);
        this.primaryLanguage = primaryLanguage;
    }

    /**
     * Get the detected primary language.
     *
     * @return The detected language.
     */
    public DetectedLanguage getPrimaryLanguage() {
        throwExceptionIfError();
        return primaryLanguage;
    }
}
