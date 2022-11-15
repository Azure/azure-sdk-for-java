// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.util.DynamicClassifyDocumentResultCollection;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;

/**
 * The helper class to set the non-public properties of an {@link DynamicClassifyDocumentResultCollection} instance.
 */
public final class DynamicClassifyDocumentResultCollectionPropertiesHelper {
    private static DynamicClassifyDocumentResultCollectionAccessor accessor;

    private DynamicClassifyDocumentResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DynamicClassifyDocumentResultCollection}
     * instance.
     */
    public interface DynamicClassifyDocumentResultCollectionAccessor {
        void setModelVersion(DynamicClassifyDocumentResultCollection resultCollection, String modelVersion);
        void setStatistics(DynamicClassifyDocumentResultCollection resultCollection,
                           TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link DynamicClassifyDocumentResultCollection} to set it's accessor.
     *
     * @param classifyDocumentResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final DynamicClassifyDocumentResultCollectionAccessor classifyDocumentResultCollectionAccessor) {
        accessor = classifyDocumentResultCollectionAccessor;
    }

    public static void setModelVersion(DynamicClassifyDocumentResultCollection resultCollection, String modelVersion) {
        accessor.setModelVersion(resultCollection, modelVersion);
    }

    public static void setStatistics(DynamicClassifyDocumentResultCollection resultCollection,
                                     TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
