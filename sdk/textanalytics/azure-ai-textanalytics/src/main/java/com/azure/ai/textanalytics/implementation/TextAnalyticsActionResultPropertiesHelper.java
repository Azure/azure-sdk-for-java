// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextAnalyticsActionResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;

import java.time.OffsetDateTime;

/**
 * The helper class to set the non-public properties of an {@link TextAnalyticsActionResult} instance.
 */
public final class TextAnalyticsActionResultPropertiesHelper {
    private static TextAnalyticsActionResultPropertiesHelper.TextAnalyticsActionResultAccessor accessor;

    private TextAnalyticsActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TextAnalyticsActionResult}
     * instance.
     */
    public interface TextAnalyticsActionResultAccessor {
        void setCompletedAt(TextAnalyticsActionResult actionResult, OffsetDateTime completedAt);
        void setError(TextAnalyticsActionResult actionResult, TextAnalyticsError error);
        void setIsError(TextAnalyticsActionResult actionResult, boolean isError);
    }

    /**
     * The method called from {@link TextAnalyticsActionResult} to set it's accessor.
     *
     * @param textAnalyticsActionResultAccessor The accessor.
     */
    public static void setAccessor(final TextAnalyticsActionResultPropertiesHelper.TextAnalyticsActionResultAccessor
                                       textAnalyticsActionResultAccessor) {
        accessor = textAnalyticsActionResultAccessor;
    }

    public static void setCompletedAt(TextAnalyticsActionResult actionResult, OffsetDateTime completedAt) {
        accessor.setCompletedAt(actionResult, completedAt);
    }

    public static void setError(TextAnalyticsActionResult actionResult, TextAnalyticsError error) {
        accessor.setError(actionResult, error);
    }

    public static void setIsError(TextAnalyticsActionResult actionResult, boolean isError) {
        accessor.setIsError(actionResult, isError);
    }
}
