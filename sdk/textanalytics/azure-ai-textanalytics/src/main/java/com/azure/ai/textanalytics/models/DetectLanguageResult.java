// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The DetectedLanguageResult model.
 */
@Fluent
public final class DetectLanguageResult extends DocumentResult {
    private DetectedLanguage primaryLanguage;
    private List<DetectedLanguage> detectedLanguages;

    public DetectLanguageResult(String id, TextDocumentStatistics textDocumentStatistics, Error error,
                                DetectedLanguage primaryLanguage, List<DetectedLanguage> detectedLanguages) {
        super(id, textDocumentStatistics, error);
        this.primaryLanguage = primaryLanguage;
        this.detectedLanguages = detectedLanguages;
    }

    public DetectedLanguage getPrimaryLanguage() {
        return primaryLanguage;
    }

    DetectLanguageResult setPrimaryLanguage(DetectedLanguage detectedLanguage) {
        this.primaryLanguage = detectedLanguage;
        return this;
    }

    public List<DetectedLanguage> getDetectedLanguages() {
        return detectedLanguages;
    }

    DetectLanguageResult setDetectedLanguages(List<DetectedLanguage> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
        return this;
    }
}
