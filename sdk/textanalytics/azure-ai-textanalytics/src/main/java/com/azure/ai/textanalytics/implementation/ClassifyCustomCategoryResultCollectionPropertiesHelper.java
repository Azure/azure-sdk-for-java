// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ClassifyDocumentSingleCategoryResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyDocumentSingleCategoryResultCollection} instance.
 */
public final class ClassifyCustomCategoryResultCollectionPropertiesHelper {
    private static ClassifyCustomCategoryResultCollectionAccessor accessor;

    private ClassifyCustomCategoryResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyDocumentSingleCategoryResultCollection}
     * instance.
     */
    public interface ClassifyCustomCategoryResultCollectionAccessor {
        void setProjectName(ClassifyDocumentSingleCategoryResultCollection resultCollection, String projectName);
        void setDeploymentName(ClassifyDocumentSingleCategoryResultCollection resultCollection, String deploymentName);
        void setStatistics(ClassifyDocumentSingleCategoryResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ClassifyDocumentSingleCategoryResultCollection} to set it's accessor.
     *
     * @param classifyCustomCategoryResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyCustomCategoryResultCollectionAccessor classifyCustomCategoryResultCollectionAccessor) {
        accessor = classifyCustomCategoryResultCollectionAccessor;
    }

    public static void setProjectName(ClassifyDocumentSingleCategoryResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(ClassifyDocumentSingleCategoryResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(ClassifyDocumentSingleCategoryResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
