// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ExtractiveSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ExtractiveSummaryResultCollection} instance.
 */
public final class ExtractiveSummaryResultCollectionPropertiesHelper {
    private static ExtractiveSummaryResultCollectionAccessor accessor;

    private ExtractiveSummaryResultCollectionPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link ExtractiveSummaryResultCollection} instance.
     */
    public interface ExtractiveSummaryResultCollectionAccessor {
        void setModelVersion(ExtractiveSummaryResultCollection resultCollection, String modelVersion);

        void setStatistics(ExtractiveSummaryResultCollection resultCollection, TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ExtractiveSummaryResultCollection} to set it's accessor.
     *
     * @param extractiveSummaryResultCollectionAccessor The accessor.
     */
    public static void
        setAccessor(final ExtractiveSummaryResultCollectionAccessor extractiveSummaryResultCollectionAccessor) {
        accessor = extractiveSummaryResultCollectionAccessor;
    }

    public static void setModelVersion(ExtractiveSummaryResultCollection resultCollection, String modelVersion) {
        accessor.setModelVersion(resultCollection, modelVersion);
    }

    public static void setStatistics(ExtractiveSummaryResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
