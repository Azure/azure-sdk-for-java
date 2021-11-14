// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.MultiCategoryClassifyResultCollection;

/**
 * The helper class to set the non-public properties of an {@link MultiCategoryClassifyResultCollection} instance.
 */
public final class MultiCategoryClassifyResultCollectionPropertiesHelper {
    private static MultiCategoryClassifyResultCollectionAccessor accessor;

    private MultiCategoryClassifyResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link MultiCategoryClassifyResultCollection}
     * instance.
     */
    public interface MultiCategoryClassifyResultCollectionAccessor {
        void setProjectName(MultiCategoryClassifyResultCollection resultCollection, String projectName);
        void setDeploymentName(MultiCategoryClassifyResultCollection resultCollection, String deploymentName);
        void setStatistics(MultiCategoryClassifyResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link MultiCategoryClassifyResultCollection} to set it's accessor.
     *
     * @param multiCategoryClassifyResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final MultiCategoryClassifyResultCollectionAccessor multiCategoryClassifyResultCollectionAccessor) {
        accessor = multiCategoryClassifyResultCollectionAccessor;
    }

    public static void setProjectName(MultiCategoryClassifyResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(MultiCategoryClassifyResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(MultiCategoryClassifyResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
