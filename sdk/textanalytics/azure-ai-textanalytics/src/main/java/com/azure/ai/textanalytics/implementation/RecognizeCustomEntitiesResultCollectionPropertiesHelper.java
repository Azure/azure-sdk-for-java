// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.RecognizeCustomEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link RecognizeCustomEntitiesResultCollection} instance.
 */
public final class RecognizeCustomEntitiesResultCollectionPropertiesHelper {
    private static RecognizeCustomEntitiesResultCollectionAccessor accessor;

    private RecognizeCustomEntitiesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizeCustomEntitiesResultCollection}
     * instance.
     */
    public interface RecognizeCustomEntitiesResultCollectionAccessor {
        void setProjectName(RecognizeCustomEntitiesResultCollection resultCollection, String projectName);
        void setDeploymentName(RecognizeCustomEntitiesResultCollection resultCollection, String deploymentName);
        void setStatistics(RecognizeCustomEntitiesResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link RecognizeCustomEntitiesResultCollection} to set it's accessor.
     *
     * @param recognizeCustomEntitiesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeCustomEntitiesResultCollectionAccessor recognizeCustomEntitiesResultCollectionAccessor) {
        accessor = recognizeCustomEntitiesResultCollectionAccessor;
    }

    public static void setProjectName(RecognizeCustomEntitiesResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(RecognizeCustomEntitiesResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(RecognizeCustomEntitiesResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
