// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.CustomRecognizeEntitiesActionResult;
import com.azure.ai.textanalytics.util.CustomRecognizeEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomRecognizeEntitiesActionResult} instance.
 */
public final class CustomRecognizeEntitiesActionResultPropertiesHelper {
    private static RecognizeCustomEntitiesActionResultAccessor accessor;

    private CustomRecognizeEntitiesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomRecognizeEntitiesActionResult}
     * instance.
     */
    public interface RecognizeCustomEntitiesActionResultAccessor {
        void setDocumentsResults(CustomRecognizeEntitiesActionResult actionResult,
            CustomRecognizeEntitiesResultCollection documentsResults);
    }

    /**
     * The method called from {@link CustomRecognizeEntitiesActionResult} to set it's accessor.
     *
     * @param recognizeCustomEntitiesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeCustomEntitiesActionResultAccessor recognizeCustomEntitiesActionResultAccessor) {
        accessor = recognizeCustomEntitiesActionResultAccessor;
    }

    public static void setDocumentsResults(CustomRecognizeEntitiesActionResult actionResult,
        CustomRecognizeEntitiesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
