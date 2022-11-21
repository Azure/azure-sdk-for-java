// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractKeyPhraseResult;
import com.azure.ai.textanalytics.models.DetectedLanguage;

/**
 * The helper class to set the non-public properties of an {@link ExtractKeyPhraseResult} instance.
 */
public final class ExtractKeyPhraseResultPropertiesHelper {
    private static ExtractKeyPhraseResultAccessor accessor;

    private ExtractKeyPhraseResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractKeyPhraseResult}
     * instance.
     */
    public interface ExtractKeyPhraseResultAccessor {
        void setDetectedLanguage(ExtractKeyPhraseResult documentResult, DetectedLanguage detectedLanguage);
    }

    /**
     * The method called from {@link ExtractKeyPhraseResult} to set it's accessor.
     *
     * @param extractKeyPhraseResultAccessor The accessor.
     */
    public static void setAccessor(
        final ExtractKeyPhraseResultAccessor extractKeyPhraseResultAccessor) {
        accessor = extractKeyPhraseResultAccessor;
    }

    public static void setDetectedLanguage(ExtractKeyPhraseResult documentResult, DetectedLanguage detectedLanguage) {
        accessor.setDetectedLanguage(documentResult, detectedLanguage);
    }
}
