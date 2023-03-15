// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.AbstractSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link AbstractSummaryResultCollection} instance.
 */
public final class AbstractSummaryResultCollectionPropertiesHelper {
    private static AbstractSummaryResultCollectionAccessor accessor;

    private AbstractSummaryResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link AbstractSummaryResultCollection} instance.
     */
    public interface AbstractSummaryResultCollectionAccessor {
        void setModelVersion(AbstractSummaryResultCollection resultCollection, String modelVersion);
        void setStatistics(AbstractSummaryResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link AbstractSummaryResultCollection} to set it's accessor.
     *
     * @param abstractSummaryResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final AbstractSummaryResultCollectionAccessor abstractSummaryResultCollectionAccessor) {
        accessor = abstractSummaryResultCollectionAccessor;
    }

    public static void setModelVersion(
        AbstractSummaryResultCollection resultCollection, String modelVersion) {
        accessor.setModelVersion(resultCollection, modelVersion);
    }

    public static void setStatistics(
        AbstractSummaryResultCollection resultCollection, TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
