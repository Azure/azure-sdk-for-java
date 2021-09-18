// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.CustomRecognizeEntitiesResultCollection;

/**
 * The helper class to set the non-public properties of an {@link CustomRecognizeEntitiesResultCollection} instance.
 */
public final class RecognizeCustomEntitiesResultCollectionPropertiesHelper {
    private static RecognizeCustomEntitiesResultCollectionAccessor accessor;

    private RecognizeCustomEntitiesResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link CustomRecognizeEntitiesResultCollection}
     * instance.
     */
    public interface RecognizeCustomEntitiesResultCollectionAccessor {
        void setProjectName(CustomRecognizeEntitiesResultCollection resultCollection, String projectName);
        void setDeploymentName(CustomRecognizeEntitiesResultCollection resultCollection, String deploymentName);
        void setStatistics(CustomRecognizeEntitiesResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link CustomRecognizeEntitiesResultCollection} to set it's accessor.
     *
     * @param recognizeCustomEntitiesResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final RecognizeCustomEntitiesResultCollectionAccessor recognizeCustomEntitiesResultCollectionAccessor) {
        accessor = recognizeCustomEntitiesResultCollectionAccessor;
    }

    public static void setProjectName(CustomRecognizeEntitiesResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(CustomRecognizeEntitiesResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(CustomRecognizeEntitiesResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
