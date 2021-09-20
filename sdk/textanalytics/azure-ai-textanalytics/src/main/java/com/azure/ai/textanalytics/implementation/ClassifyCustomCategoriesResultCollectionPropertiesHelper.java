// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ClassifyCustomMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyCustomMultiCategoriesResultCollection} instance.
 */
public final class ClassifyCustomCategoriesResultCollectionPropertiesHelper {
    private static ClassifyCustomCategoriesResultCollectionAccessor accessor;

    private ClassifyCustomCategoriesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyCustomMultiCategoriesResultCollection}
     * instance.
     */
    public interface ClassifyCustomCategoriesResultCollectionAccessor {
        void setProjectName(ClassifyCustomMultiCategoriesResultCollection resultCollection, String projectName);
        void setDeploymentName(ClassifyCustomMultiCategoriesResultCollection resultCollection, String deploymentName);
        void setStatistics(ClassifyCustomMultiCategoriesResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ClassifyCustomMultiCategoriesResultCollection} to set it's accessor.
     *
     * @param classifyCustomCategoriesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoriesResultCollectionAccessor classifyCustomCategoriesResultCollectionAccessor) {
        accessor = classifyCustomCategoriesResultCollectionAccessor;
    }

    public static void setProjectName(ClassifyCustomMultiCategoriesResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(ClassifyCustomMultiCategoriesResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(ClassifyCustomMultiCategoriesResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
