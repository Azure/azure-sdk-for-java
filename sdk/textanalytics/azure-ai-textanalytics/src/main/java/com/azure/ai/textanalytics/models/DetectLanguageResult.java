// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * The DetectedLanguageResult model.
 */
@Immutable
public final class DetectLanguageResult extends DocumentResult {
    private final DetectedLanguage primaryLanguage;
    private final List<DetectedLanguage> detectedLanguages;

    /**
     * Create a {@code DetectedLanguageResult} model that describes detected languages result
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics text document statistics
     * @param error the document error.
     * @param primaryLanguage the detected primary language
     * @param detectedLanguages a list of detected language result
     */
    public DetectLanguageResult(String id, TextDocumentStatistics textDocumentStatistics, TextAnalyticsError error,
                                DetectedLanguage primaryLanguage, List<DetectedLanguage> detectedLanguages) {
        super(id, textDocumentStatistics, error);
        this.primaryLanguage = primaryLanguage;
        this.detectedLanguages = detectedLanguages == null ? new ArrayList<>() : detectedLanguages;
    }

    /**
     * Get the detected primary language
     *
     * @return the detected language
     */
    public DetectedLanguage getPrimaryLanguage() {
        throwExceptionIfError();
        return primaryLanguage;
    }

    /**
     * Get the list of detected languages
     *
     * @return the list of detected language
     */
    public List<DetectedLanguage> getDetectedLanguages() {
        throwExceptionIfError();
        return detectedLanguages;
    }
}
