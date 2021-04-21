// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.RecognizePiiEntitiesActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.util.RecognizePiiEntitiesResultCollection;

import java.time.OffsetDateTime;

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
        void setCompletedAt(RecognizePiiEntitiesActionResult actionsResult, OffsetDateTime completedAt);
        void setError(RecognizePiiEntitiesActionResult actionResult, TextAnalyticsError error);
        void setIsError(RecognizePiiEntitiesActionResult actionResult, boolean isError);
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

    public static void setCompletedAt(RecognizePiiEntitiesActionResult actionsResult, OffsetDateTime completedAt) {
        accessor.setCompletedAt(actionsResult, completedAt);
    }

    public static void setError(RecognizePiiEntitiesActionResult actionResult, TextAnalyticsError error) {
        accessor.setError(actionResult, error);
    }

    public static void setIsError(RecognizePiiEntitiesActionResult actionResult, boolean isError) {
        accessor.setIsError(actionResult, isError);
    }

    public static void setResult(RecognizePiiEntitiesActionResult actionResult,
        RecognizePiiEntitiesResultCollection result) {
        accessor.setResult(actionResult, result);
    }
}
