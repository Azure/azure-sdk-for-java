// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.AbstractiveSummaryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link AbstractiveSummaryResultCollection} instance.
 */
public final class AbstractiveSummaryResultCollectionPropertiesHelper {
    private static AbstractiveSummaryResultCollectionAccessor accessor;

    private AbstractiveSummaryResultCollectionPropertiesHelper() {
    }

    /**
     * Type defining the methods to set the non-public properties of an
     * {@link AbstractiveSummaryResultCollection} instance.
     */
    public interface AbstractiveSummaryResultCollectionAccessor {
        void setModelVersion(AbstractiveSummaryResultCollection resultCollection, String modelVersion);

        void setStatistics(AbstractiveSummaryResultCollection resultCollection, TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link AbstractiveSummaryResultCollection} to set it's accessor.
     *
     * @param abstractiveSummaryResultCollectionAccessor The accessor.
     */
    public static void
        setAccessor(final AbstractiveSummaryResultCollectionAccessor abstractiveSummaryResultCollectionAccessor) {
        accessor = abstractiveSummaryResultCollectionAccessor;
    }

    public static void setModelVersion(AbstractiveSummaryResultCollection resultCollection, String modelVersion) {
        accessor.setModelVersion(resultCollection, modelVersion);
    }

    public static void setStatistics(AbstractiveSummaryResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
