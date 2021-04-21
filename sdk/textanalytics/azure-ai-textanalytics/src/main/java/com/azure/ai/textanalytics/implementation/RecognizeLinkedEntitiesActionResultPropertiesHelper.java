// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.RecognizeLinkedEntitiesActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.util.RecognizeLinkedEntitiesResultCollection;

import java.time.OffsetDateTime;

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
        void setCompletedAt(RecognizeLinkedEntitiesActionResult actionsResult, OffsetDateTime completedAt);
        void setError(RecognizeLinkedEntitiesActionResult actionResult, TextAnalyticsError error);
        void setIsError(RecognizeLinkedEntitiesActionResult actionResult, boolean isError);
        void setResult(RecognizeLinkedEntitiesActionResult actionsResult, RecognizeLinkedEntitiesResultCollection result);
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

    public static void setCompletedAt(RecognizeLinkedEntitiesActionResult actionsResult, OffsetDateTime completedAt) {
        accessor.setCompletedAt(actionsResult, completedAt);
    }

    public static void setError(RecognizeLinkedEntitiesActionResult actionResult, TextAnalyticsError error) {
        accessor.setError(actionResult, error);
    }

    public static void setIsError(RecognizeLinkedEntitiesActionResult actionResult, boolean isError) {
        accessor.setIsError(actionResult, isError);
    }

    public static void setResult(RecognizeLinkedEntitiesActionResult actionResult,
        RecognizeLinkedEntitiesResultCollection result) {
        accessor.setResult(actionResult, result);
    }
}
