// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.textanalytics.models;

/**
 * The {@link DetectLanguageResult} model.
 */
public interface DetectLanguageResult extends TextAnalyticsResult {
    /**
     * Get the detected primary language.
     *
     * @return The detected language.
     */
    DetectedLanguage getPrimaryLanguage();
}
