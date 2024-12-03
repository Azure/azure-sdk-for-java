// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.ClassifyDocumentResultCollection;

/**
 * The helper class to set the non-public properties of an {@link ClassifyDocumentResultCollection} instance.
 */
public final class ClassifyDocumentResultCollectionPropertiesHelper {
    private static ClassifyDocumentResultCollectionAccessor accessor;

    private ClassifyDocumentResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ClassifyDocumentResultCollection}
     * instance.
     */
    public interface ClassifyDocumentResultCollectionAccessor {
        void setProjectName(ClassifyDocumentResultCollection resultCollection, String projectName);
        void setDeploymentName(ClassifyDocumentResultCollection resultCollection, String deploymentName);
        void setStatistics(ClassifyDocumentResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link ClassifyDocumentResultCollection} to set it's accessor.
     *
     * @param classifyDocumentResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final ClassifyDocumentResultCollectionAccessor classifyDocumentResultCollectionAccessor) {
        accessor = classifyDocumentResultCollectionAccessor;
    }

    public static void setProjectName(ClassifyDocumentResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(ClassifyDocumentResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(ClassifyDocumentResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
