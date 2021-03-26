// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link RecognizePiiEntitiesActionResult} instance.
 */
public final class RecognizePiiEntitiesActionResultPropertiesHelper {
    private static RecognizePiiEntitiesActionResultAccessor accessor;

    private RecognizePiiEntitiesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizePiiEntitiesActionResult}
     * instance.
     */
    public interface RecognizePiiEntitiesActionResultAccessor {
        void setResult(RecognizePiiEntitiesActionResult actionResult, RecognizePiiEntitiesResultCollection result);
    }

    /**
     * The method called from {@link RecognizePiiEntitiesActionResult} to set it's accessor.
     *
     * @param recognizePiiEntitiesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizePiiEntitiesActionResultAccessor recognizePiiEntitiesActionResultAccessor) {
        accessor = recognizePiiEntitiesActionResultAccessor;
    }

    public static void setResult(RecognizePiiEntitiesActionResult actionResult,
        RecognizePiiEntitiesResultCollection result) {
        accessor.setResult(actionResult, result);
    }
}
