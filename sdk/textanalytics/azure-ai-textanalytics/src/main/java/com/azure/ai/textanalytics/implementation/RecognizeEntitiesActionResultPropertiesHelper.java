// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.RecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.util.RecognizeEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link RecognizeEntitiesActionResult} instance.
 */
public final class RecognizeEntitiesActionResultPropertiesHelper {
    private static RecognizeEntitiesActionResultAccessor accessor;

    private RecognizeEntitiesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizeEntitiesActionResult}
     * instance.
     */
    public interface RecognizeEntitiesActionResultAccessor {
        void setDocumentsResults(RecognizeEntitiesActionResult actionResult,
            RecognizeEntitiesResultCollection documentsResults);
    }

    /**
     * The method called from {@link RecognizeEntitiesActionResult} to set it's accessor.
     *
     * @param recognizeEntitiesActionResultAccessor The accessor.
     */
    public static void setAccessor(final RecognizeEntitiesActionResultAccessor recognizeEntitiesActionResultAccessor) {
        accessor = recognizeEntitiesActionResultAccessor;
    }

    public static void setDocumentsResults(RecognizeEntitiesActionResult actionResult,
        RecognizeEntitiesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
