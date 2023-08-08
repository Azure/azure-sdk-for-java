// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.RecognizeCustomEntitiesActionResult;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link RecognizeCustomEntitiesActionResult} instance.
 */
public final class RecognizeCustomEntitiesActionResultPropertiesHelper {
    private static RecognizeCustomEntitiesActionResultAccessor accessor;

    private RecognizeCustomEntitiesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizeCustomEntitiesActionResult}
     * instance.
     */
    public interface RecognizeCustomEntitiesActionResultAccessor {
        void setDocumentsResults(RecognizeCustomEntitiesActionResult actionResult,
            RecognizeCustomEntitiesResultCollection documentsResults);
    }

    /**
     * The method called from {@link RecognizeCustomEntitiesActionResult} to set it's accessor.
     *
     * @param recognizeCustomEntitiesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeCustomEntitiesActionResultAccessor recognizeCustomEntitiesActionResultAccessor) {
        accessor = recognizeCustomEntitiesActionResultAccessor;
    }

    public static void setDocumentsResults(RecognizeCustomEntitiesActionResult actionResult,
        RecognizeCustomEntitiesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
