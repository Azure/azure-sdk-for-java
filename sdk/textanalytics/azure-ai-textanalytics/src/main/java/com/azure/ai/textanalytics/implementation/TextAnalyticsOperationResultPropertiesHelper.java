// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextAnalyticsOperationResult;

/**
 * The helper class to set the non-public properties of an {@link TextAnalyticsOperationResult} instance.
 */
public final class TextAnalyticsOperationResultPropertiesHelper {
    private static TextAnalyticsOperationResultAccessor accessor;

    private TextAnalyticsOperationResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TextAnalyticsOperationResult}
     * instance.
     */
    public interface TextAnalyticsOperationResultAccessor {
        void setResultId(TextAnalyticsOperationResult entitiesResult, String resultId);
    }

    /**
     * The method called from {@link TextAnalyticsOperationResult} to set it's accessor.
     *
     * @param textAnalyticsOperationResultAccessor The accessor.
     */
    public static void setAccessor(
        final TextAnalyticsOperationResultAccessor textAnalyticsOperationResultAccessor) {
        accessor = textAnalyticsOperationResultAccessor;
    }

    public static void setResultId(TextAnalyticsOperationResult operationResult, String resultId) {
        accessor.setResultId(operationResult, resultId);
    }
}
