// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ClassifyCustomCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomCategoriesResultCollection} instance.
 */
public final class ClassifyCustomCategoriesResultCollectionPropertiesHelper {
    private static ClassifyCustomCategoriesResultCollectionAccessor accessor;

    private ClassifyCustomCategoriesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomCategoriesResultCollection}
     * instance.
     */
    public interface ClassifyCustomCategoriesResultCollectionAccessor {
        void setProjectName(ClassifyCustomCategoriesResultCollection resultCollection, String projectName);
        void setDeploymentName(ClassifyCustomCategoriesResultCollection resultCollection, String deploymentName);
        void setStatistics(ClassifyCustomCategoriesResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ClassifyCustomCategoriesResultCollection} to set it's accessor.
     *
     * @param classifyCustomCategoriesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoriesResultCollectionAccessor classifyCustomCategoriesResultCollectionAccessor) {
        accessor = classifyCustomCategoriesResultCollectionAccessor;
    }

    public static void setProjectName(ClassifyCustomCategoriesResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(ClassifyCustomCategoriesResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(ClassifyCustomCategoriesResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
