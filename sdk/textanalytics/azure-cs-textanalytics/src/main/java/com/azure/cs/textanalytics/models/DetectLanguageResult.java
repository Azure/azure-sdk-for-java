// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * The DetectedLanguageResult model.
 */
@Fluent
public final class DetectLanguageResult extends DocumentResult {
    private DetectedLanguage primaryLanguage;
    private List<DetectedLanguage> detectedLanguages;

    public DetectLanguageResult(String id, Error error, boolean isError) {
        super(id, error, isError);
    }

    public DetectLanguageResult(String id, TextDocumentStatistics textDocumentStatistics,
                                DetectedLanguage primaryLanguage, List<DetectedLanguage> detectedLanguages) {
        super(id, textDocumentStatistics);
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
