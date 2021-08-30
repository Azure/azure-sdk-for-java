// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ClassifyMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyMultiCategoriesResultCollection} instance.
 */
public final class ClassifyMultiCategoriesResultCollectionPropertiesHelper {
    private static ClassifyMultiCategoriesResultCollectionAccessor accessor;

    private ClassifyMultiCategoriesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyMultiCategoriesResultCollection}
     * instance.
     */
    public interface ClassifyMultiCategoriesResultCollectionAccessor {
        void setProjectName(ClassifyMultiCategoriesResultCollection resultCollection, String projectName);
        void setDeploymentName(ClassifyMultiCategoriesResultCollection resultCollection, String deploymentName);
        void setStatistics(ClassifyMultiCategoriesResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ClassifyMultiCategoriesResultCollection} to set it's accessor.
     *
     * @param classifyMultiCategoriesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyMultiCategoriesResultCollectionAccessor classifyMultiCategoriesResultCollectionAccessor) {
        accessor = classifyMultiCategoriesResultCollectionAccessor;
    }

    public static void setProjectName(ClassifyMultiCategoriesResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(ClassifyMultiCategoriesResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(ClassifyMultiCategoriesResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
