// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;

import java.util.List;

/**
 * The DetectedLanguageResult model.
 */
@Fluent
public final class DetectedLanguageResult extends DocumentResult {
    private DetectedLanguage primaryLanguage;
    private List<DetectedLanguage> detectedLanguages;

    public DetectedLanguage getPrimaryLanguage() {
        return primaryLanguage;
    }

    DetectedLanguageResult setPrimaryLanguage(DetectedLanguage detectedLanguage) {
        this.primaryLanguage = detectedLanguage;
        return this;
    }

    public List<DetectedLanguage> getDetectedLanguages() {
        return detectedLanguages;
    }

    DetectedLanguageResult setDetectedLanguages(List<DetectedLanguage> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
        return this;
    }
}
