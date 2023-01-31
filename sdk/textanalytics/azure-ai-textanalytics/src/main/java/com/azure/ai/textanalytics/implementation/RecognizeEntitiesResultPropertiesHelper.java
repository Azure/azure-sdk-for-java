// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.RecognizeEntitiesResult;

/**
 * The helper class to set the non-public properties of an {@link RecognizeEntitiesResult} instance.
 */
public final class RecognizeEntitiesResultPropertiesHelper {
    private static RecognizeEntitiesResultAccessor accessor;

    private RecognizeEntitiesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizeEntitiesResult}
     * instance.
     */
    public interface RecognizeEntitiesResultAccessor {
        void setDetectedLanguage(RecognizeEntitiesResult documentResult, DetectedLanguage detectedLanguage);
    }

    /**
     * The method called from {@link RecognizeEntitiesResult} to set it's accessor.
     *
     * @param recognizeEntitiesResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeEntitiesResultAccessor recognizeEntitiesResultAccessor) {
        accessor = recognizeEntitiesResultAccessor;
    }

    public static void setDetectedLanguage(RecognizeEntitiesResult documentResult,
        DetectedLanguage detectedLanguage) {
        accessor.setDetectedLanguage(documentResult, detectedLanguage);
    }
}
