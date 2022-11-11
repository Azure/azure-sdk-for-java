// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesResult;

/**
 * The helper class to set the non-public properties of an {@link RecognizeLinkedEntitiesResult} instance.
 */
public final class RecognizeLinkedEntitiesResultPropertiesHelper {
    private static RecognizeLinkedEntitiesResultAccessor accessor;

    private RecognizeLinkedEntitiesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizeLinkedEntitiesResult}
     * instance.
     */
    public interface RecognizeLinkedEntitiesResultAccessor {
        void setDetectedLanguage(RecognizeLinkedEntitiesResult documentResult, DetectedLanguage detectedLanguage);
    }

    /**
     * The method called from {@link RecognizeLinkedEntitiesResult} to set it's accessor.
     *
     * @param recognizeLinkedEntitiesResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeLinkedEntitiesResultAccessor recognizeLinkedEntitiesResultAccessor) {
        accessor = recognizeLinkedEntitiesResultAccessor;
    }

    public static void setDetectedLanguage(RecognizeLinkedEntitiesResult documentResult,
        DetectedLanguage detectedLanguage) {
        accessor.setDetectedLanguage(documentResult, detectedLanguage);
    }
}
