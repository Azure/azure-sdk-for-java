// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.ExtractKeyPhrasesActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.util.ExtractKeyPhrasesResultCollection;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link ExtractKeyPhrasesActionResult} instance.
 */
public final class ExtractKeyPhrasesActionResultPropertiesHelper {
    private static ExtractKeyPhrasesActionResultAccessor accessor;

    private ExtractKeyPhrasesActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ExtractKeyPhrasesActionResult}
     * instance.
     */
    public interface ExtractKeyPhrasesActionResultAccessor {
        void setCompletedAt(ExtractKeyPhrasesActionResult actionsResult, OffsetDateTime completedAt);
        void setError(ExtractKeyPhrasesActionResult actionResult, TextAnalyticsError error);
        void setIsError(ExtractKeyPhrasesActionResult actionResult, boolean isError);
        void setResult(ExtractKeyPhrasesActionResult actionsResult, ExtractKeyPhrasesResultCollection result);
    }

    /**
     * The method called from {@link ExtractKeyPhrasesActionResult} to set it's accessor.
     *
     * @param extractKeyPhrasesActionResultAccessor The accessor.
     */
    public static void setAccessor(final ExtractKeyPhrasesActionResultAccessor extractKeyPhrasesActionResultAccessor) {
        accessor = extractKeyPhrasesActionResultAccessor;
    }

    public static void setCompletedAt(ExtractKeyPhrasesActionResult actionsResult, OffsetDateTime completedAt) {
        accessor.setCompletedAt(actionsResult, completedAt);
    }

    public static void setError(ExtractKeyPhrasesActionResult actionResult, TextAnalyticsError error) {
        accessor.setError(actionResult, error);
    }

    public static void setIsError(ExtractKeyPhrasesActionResult actionResult, boolean isError) {
        accessor.setIsError(actionResult, isError);
    }

    public static void setResult(ExtractKeyPhrasesActionResult actionResult, ExtractKeyPhrasesResultCollection result) {
        accessor.setResult(actionResult, result);
    }
}
