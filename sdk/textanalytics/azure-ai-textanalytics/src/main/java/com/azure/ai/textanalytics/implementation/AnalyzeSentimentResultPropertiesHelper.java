// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.AnalyzeSentimentResult;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeSentimentResult} instance.
 */
public final class AnalyzeSentimentResultPropertiesHelper {
    private static AnalyzeSentimentResultAccessor accessor;

    private AnalyzeSentimentResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeSentimentResult}
     * instance.
     */
    public interface AnalyzeSentimentResultAccessor {
        void setDetectedLanguage(AnalyzeSentimentResult documentResult, DetectedLanguage detectedLanguage);
    }

    /**
     * The method called from {@link AnalyzeSentimentResult} to set it's accessor.
     *
     * @param analyzeSentimentResultAccessor The accessor.
     */
    public static void setAccessor(
        final AnalyzeSentimentResultAccessor analyzeSentimentResultAccessor) {
        accessor = analyzeSentimentResultAccessor;
    }

    public static void setDetectedLanguage(AnalyzeSentimentResult documentResult, DetectedLanguage detectedLanguage) {
        accessor.setDetectedLanguage(documentResult, detectedLanguage);
    }
}
