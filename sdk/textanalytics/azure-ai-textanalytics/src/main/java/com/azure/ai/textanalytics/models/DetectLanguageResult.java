// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The DetectedLanguageResult model.
 */
@Immutable
public final class DetectLanguageResult extends DocumentResult {
    private final DetectedLanguage primaryLanguage;

    /**
     * Create a {@code DetectedLanguageResult} model that describes detected languages result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics text document statistics
     * @param error the document error.
     * @param primaryLanguage the detected primary language
     */
    public DetectLanguageResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error,
                                DetectedLanguage primaryLanguage) {
        super(id, textDocumentStatistics, error);
        this.primaryLanguage = primaryLanguage;
    }

    /**
     * Get the detected primary language.
     *
     * @return the detected language
     */
    public DetectedLanguage getPrimaryLanguage() {
        throwExceptionIfError();
        return primaryLanguage;
    }
}
