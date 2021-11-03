// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.SingleCategoryClassifyResultCollection;

/**
 * The helper class to set the non-public properties of an {@link SingleCategoryClassifyResultCollection} instance.
 */
public final class SingleCategoryClassifyResultCollectionPropertiesHelper {
    private static SingleCategoryClassifyResultCollectionAccessor accessor;

    private SingleCategoryClassifyResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SingleCategoryClassifyResultCollection}
     * instance.
     */
    public interface SingleCategoryClassifyResultCollectionAccessor {
        void setProjectName(SingleCategoryClassifyResultCollection resultCollection, String projectName);
        void setDeploymentName(SingleCategoryClassifyResultCollection resultCollection, String deploymentName);
        void setStatistics(SingleCategoryClassifyResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link SingleCategoryClassifyResultCollection} to set it's accessor.
     *
     * @param singleCategoryClassifyResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final SingleCategoryClassifyResultCollectionAccessor singleCategoryClassifyResultCollectionAccessor) {
        accessor = singleCategoryClassifyResultCollectionAccessor;
    }

    public static void setProjectName(SingleCategoryClassifyResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(SingleCategoryClassifyResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(SingleCategoryClassifyResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
