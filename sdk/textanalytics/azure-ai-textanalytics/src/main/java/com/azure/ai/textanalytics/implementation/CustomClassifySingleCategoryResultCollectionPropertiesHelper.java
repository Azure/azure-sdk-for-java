// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.CustomClassifySingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifySingleCategoryResultCollection} instance.
 */
public final class CustomClassifySingleCategoryResultCollectionPropertiesHelper {
    private static ClassifySingleCategoryResultCollectionAccessor accessor;

    private CustomClassifySingleCategoryResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifySingleCategoryResultCollection}
     * instance.
     */
    public interface ClassifySingleCategoryResultCollectionAccessor {
        void setProjectName(CustomClassifySingleCategoryResultCollection resultCollection, String projectName);
        void setDeploymentName(CustomClassifySingleCategoryResultCollection resultCollection, String deploymentName);
        void setStatistics(CustomClassifySingleCategoryResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link CustomClassifySingleCategoryResultCollection} to set it's accessor.
     *
     * @param classifySingleCategoryResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifySingleCategoryResultCollectionAccessor classifySingleCategoryResultCollectionAccessor) {
        accessor = classifySingleCategoryResultCollectionAccessor;
    }

    public static void setProjectName(CustomClassifySingleCategoryResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(CustomClassifySingleCategoryResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(CustomClassifySingleCategoryResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
