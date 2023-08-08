// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.AnalyzeSentimentActionResult;
import com.azure.ai.textanalytics.util.AnalyzeSentimentResultCollection;

/**
 * The helper class to set the non-public properties of an {@link AnalyzeSentimentActionResult} instance.
 */
public final class AnalyzeSentimentActionResultPropertiesHelper {
    private static AnalyzeSentimentActionResultAccessor accessor;

    private AnalyzeSentimentActionResultPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AnalyzeSentimentActionResult}
     * instance.
     */
    public interface AnalyzeSentimentActionResultAccessor {
        void setDocumentsResults(AnalyzeSentimentActionResult actionResult,
            AnalyzeSentimentResultCollection documentsResults);
    }

    /**
     * The method called from {@link AnalyzeSentimentActionResult} to set it's accessor.
     *
     * @param analyzeSentimentActionResultAccessor The accessor.
     */
    public static void setAccessor(final AnalyzeSentimentActionResultAccessor analyzeSentimentActionResultAccessor) {
        accessor = analyzeSentimentActionResultAccessor;
    }

    public static void setDocumentsResults(AnalyzeSentimentActionResult actionResult,
        AnalyzeSentimentResultCollection documentsResults) {
        accessor.setDocumentsResults(actionResult, documentsResults);
    }
}
