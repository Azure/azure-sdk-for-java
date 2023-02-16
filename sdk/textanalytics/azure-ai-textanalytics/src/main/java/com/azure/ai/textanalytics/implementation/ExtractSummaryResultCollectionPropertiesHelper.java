// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ExtractSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ExtractSummaryResultCollection} instance.
 */
public final class ExtractSummaryResultCollectionPropertiesHelper {
    private static ExtractSummaryResultCollectionAccessor accessor;

    private ExtractSummaryResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link ExtractSummaryResultCollection} instance.
     */
    public interface ExtractSummaryResultCollectionAccessor {
        void setModelVersion(ExtractSummaryResultCollection resultCollection, String modelVersion);
        void setStatistics(ExtractSummaryResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ExtractSummaryResultCollection} to set it's accessor.
     *
     * @param extractSummaryResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ExtractSummaryResultCollectionAccessor extractSummaryResultCollectionAccessor) {
        accessor = extractSummaryResultCollectionAccessor;
    }

    public static void setModelVersion(
        ExtractSummaryResultCollection resultCollection, String modelVersion) {
        accessor.setModelVersion(resultCollection, modelVersion);
    }

    public static void setStatistics(
        ExtractSummaryResultCollection resultCollection, TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
