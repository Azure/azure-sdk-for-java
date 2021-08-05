// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link DetectLanguageResult} model.
 */
@Immutable
public final class DetectLanguageResult extends TextAnalyticsResult {
    private final DetectedLanguage primaryLanguage;

    /**
     * Creates a {@link DetectLanguageResult} model that describes detected languages result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param primaryLanguage The detected primary language.
     */
    public DetectLanguageResult(String id, TextDocumentStatistics textDocumentStatistics,
                                TextAnalyticsError error, DetectedLanguage primaryLanguage) {
        super(id, textDocumentStatistics, error);
        this.primaryLanguage = primaryLanguage;
    }

    /**
     * Gets the detected primary language.
     *
     * @return The detected language.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public DetectedLanguage getPrimaryLanguage() {
        throwExceptionIfError();
        return primaryLanguage;
    }
}
