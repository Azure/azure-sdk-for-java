// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ClassifySingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifySingleCategoryResultCollection} instance.
 */
public final class ClassifySingleCategoryResultCollectionPropertiesHelper {
    private static ClassifySingleCategoryResultCollectionAccessor accessor;

    private ClassifySingleCategoryResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifySingleCategoryResultCollection}
     * instance.
     */
    public interface ClassifySingleCategoryResultCollectionAccessor {
        void setProjectName(ClassifySingleCategoryResultCollection resultCollection, String projectName);
        void setDeploymentName(ClassifySingleCategoryResultCollection resultCollection, String deploymentName);
        void setStatistics(ClassifySingleCategoryResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ClassifySingleCategoryResultCollection} to set it's accessor.
     *
     * @param classifySingleCategoryResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifySingleCategoryResultCollectionAccessor classifySingleCategoryResultCollectionAccessor) {
        accessor = classifySingleCategoryResultCollectionAccessor;
    }

    public static void setProjectName(ClassifySingleCategoryResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(ClassifySingleCategoryResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(ClassifySingleCategoryResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
