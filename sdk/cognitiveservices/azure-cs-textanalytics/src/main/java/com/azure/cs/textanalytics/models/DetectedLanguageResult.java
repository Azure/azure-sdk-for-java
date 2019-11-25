// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;

/**
 * The DetectedLanguageResult model.
 */
@Fluent
public final class DetectedLanguageResult extends DocumentResult {
    private DetectedLanguage primaryLanguage;
    private IterableStream<DetectedLanguage> detectedLanguages;

    public DetectedLanguage getPrimaryLanguage() {
        return primaryLanguage;
    }

    DetectedLanguageResult setPrimaryLanguage(DetectedLanguage detectedLanguage) {
        this.primaryLanguage = detectedLanguage;
        return this;
    }

    public IterableStream<DetectedLanguage> getDetectedLanguages() {
        return detectedLanguages;
    }

    DetectedLanguageResult setDetectedLanguages(IterableStream<DetectedLanguage> detectedLanguages) {
        this.detectedLanguages = detectedLanguages;
        return this;
    }
}
