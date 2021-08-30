// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.CustomClassifyMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifyMultiCategoriesResultCollection} instance.
 */
public final class CustomClassifyMultiCategoriesResultCollectionPropertiesHelper {
    private static ClassifyMultiCategoriesResultCollectionAccessor accessor;

    private CustomClassifyMultiCategoriesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifyMultiCategoriesResultCollection}
     * instance.
     */
    public interface ClassifyMultiCategoriesResultCollectionAccessor {
        void setProjectName(CustomClassifyMultiCategoriesResultCollection resultCollection, String projectName);
        void setDeploymentName(CustomClassifyMultiCategoriesResultCollection resultCollection, String deploymentName);
        void setStatistics(CustomClassifyMultiCategoriesResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link CustomClassifyMultiCategoriesResultCollection} to set it's accessor.
     *
     * @param classifyMultiCategoriesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyMultiCategoriesResultCollectionAccessor classifyMultiCategoriesResultCollectionAccessor) {
        accessor = classifyMultiCategoriesResultCollectionAccessor;
    }

    public static void setProjectName(CustomClassifyMultiCategoriesResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(CustomClassifyMultiCategoriesResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(CustomClassifyMultiCategoriesResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
