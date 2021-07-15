// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an
 * {@link RecognizeLinkedEntitiesActionResultPropertiesHelper} instance.
 */
public final class RecognizeLinkedEntitiesActionResultPropertiesHelper {
    private static RecognizeLinkedEntitiesActionResultAccessor accessor;

    private RecognizeLinkedEntitiesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizeLinkedEntitiesActionResult}
     * instance.
     */
    public interface RecognizeLinkedEntitiesActionResultAccessor {
        void setDocumentsResults(RecognizeLinkedEntitiesActionResult actionResult,
            RecognizeLinkedEntitiesResultCollection documentsResults);
    }

    /**
     * The method called from {@link RecognizeLinkedEntitiesActionResult} to set it's accessor.
     *
     * @param recognizeLinkedEntitiesActionResultAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeLinkedEntitiesActionResultAccessor recognizeLinkedEntitiesActionResultAccessor) {
        accessor = recognizeLinkedEntitiesActionResultAccessor;
    }

    public static void setDocumentsResults(RecognizeLinkedEntitiesActionResult actionResult,
        RecognizeLinkedEntitiesResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
