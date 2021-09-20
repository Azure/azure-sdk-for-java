// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ClassifyCustomSingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomSingleCategoryResultCollection} instance.
 */
public final class ClassifyCustomCategoryResultCollectionPropertiesHelper {
    private static ClassifyCustomCategoryResultCollectionAccessor accessor;

    private ClassifyCustomCategoryResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomSingleCategoryResultCollection}
     * instance.
     */
    public interface ClassifyCustomCategoryResultCollectionAccessor {
        void setProjectName(ClassifyCustomSingleCategoryResultCollection resultCollection, String projectName);
        void setDeploymentName(ClassifyCustomSingleCategoryResultCollection resultCollection, String deploymentName);
        void setStatistics(ClassifyCustomSingleCategoryResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ClassifyCustomSingleCategoryResultCollection} to set it's accessor.
     *
     * @param classifyCustomCategoryResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoryResultCollectionAccessor classifyCustomCategoryResultCollectionAccessor) {
        accessor = classifyCustomCategoryResultCollectionAccessor;
    }

    public static void setProjectName(ClassifyCustomSingleCategoryResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(ClassifyCustomSingleCategoryResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(ClassifyCustomSingleCategoryResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
