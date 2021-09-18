// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.CustomClassifyDocumentMultiCategoriesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomClassifyDocumentMultiCategoriesResultCollection} instance.
 */
public final class ClassifyCustomCategoriesResultCollectionPropertiesHelper {
    private static ClassifyCustomCategoriesResultCollectionAccessor accessor;

    private ClassifyCustomCategoriesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomClassifyDocumentMultiCategoriesResultCollection}
     * instance.
     */
    public interface ClassifyCustomCategoriesResultCollectionAccessor {
        void setProjectName(CustomClassifyDocumentMultiCategoriesResultCollection resultCollection, String projectName);
        void setDeploymentName(CustomClassifyDocumentMultiCategoriesResultCollection resultCollection, String deploymentName);
        void setStatistics(CustomClassifyDocumentMultiCategoriesResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link CustomClassifyDocumentMultiCategoriesResultCollection} to set it's accessor.
     *
     * @param classifyCustomCategoriesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoriesResultCollectionAccessor classifyCustomCategoriesResultCollectionAccessor) {
        accessor = classifyCustomCategoriesResultCollectionAccessor;
    }

    public static void setProjectName(CustomClassifyDocumentMultiCategoriesResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(CustomClassifyDocumentMultiCategoriesResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(CustomClassifyDocumentMultiCategoriesResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
