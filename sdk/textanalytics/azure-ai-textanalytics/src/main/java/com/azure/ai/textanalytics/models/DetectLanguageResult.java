// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@DetectedLanguageResult} model.
 */
@Immutable
public final class DetectLanguageResult extends DocumentResult {
    private final DetectedLanguage primaryLanguage;

    /**
     * Create a {@code DetectedLanguageResult} model that describes detected languages result.
     *
     * @param id Unique, non-empty document identifier.
     * @param inputText The input text in request.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param primaryLanguage The detected primary language.
     */
    public DetectLanguageResult(String id, String inputText, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, DetectedLanguage primaryLanguage) {
        super(id, inputText, textDocumentStatistics, error);
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
