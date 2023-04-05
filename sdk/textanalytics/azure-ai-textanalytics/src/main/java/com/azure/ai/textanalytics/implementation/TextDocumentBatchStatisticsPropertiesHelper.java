// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;

import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link TextDocumentBatchStatistics} instance.
 */
public final class TextDocumentBatchStatisticsPropertiesHelper {
    private static TextDocumentBatchStatisticsAccessor accessor;

    private TextDocumentBatchStatisticsPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link TextDocumentBatchStatistics} instance.
     */
    public interface TextDocumentBatchStatisticsAccessor {
        void setAdditionalProperties(TextDocumentBatchStatistics batchStatistics, Map<String, Object> additionalProperties);
    }

    /**
     * The method called from {@link TextDocumentBatchStatistics} to set it's accessor.
     *
     * @param textDocumentBatchStatisticsAccessor The accessor.
     */
    public static void setAccessor(final TextDocumentBatchStatisticsAccessor textDocumentBatchStatisticsAccessor) {
        accessor = textDocumentBatchStatisticsAccessor;
    }

    public static void setAdditionalProperties(TextDocumentBatchStatistics batchStatistics,
        Map<String, Object> additionalProperties) {
        accessor.setAdditionalProperties(batchStatistics, additionalProperties);
    }
}
