// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.RecognizePiiEntitiesResult;

/**
 * The helper class to set the non-public properties of an {@link RecognizePiiEntitiesResult} instance.
 */
public final class RecognizePiiEntitiesResultPropertiesHelper {
    private static RecognizePiiEntitiesResultAccessor accessor;

    private RecognizePiiEntitiesResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizePiiEntitiesResult}
     * instance.
     */
    public interface RecognizePiiEntitiesResultAccessor {
        void setDetectedLanguage(RecognizePiiEntitiesResult documentResult, DetectedLanguage detectedLanguage);
    }

    /**
     * The method called from {@link RecognizePiiEntitiesResult} to set it's accessor.
     *
     * @param recognizePiiEntitiesResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizePiiEntitiesResultAccessor recognizePiiEntitiesResultAccessor) {
        accessor = recognizePiiEntitiesResultAccessor;
    }

    public static void setDetectedLanguage(RecognizePiiEntitiesResult documentResult,
        DetectedLanguage detectedLanguage) {
        accessor.setDetectedLanguage(documentResult, detectedLanguage);
    }
}
