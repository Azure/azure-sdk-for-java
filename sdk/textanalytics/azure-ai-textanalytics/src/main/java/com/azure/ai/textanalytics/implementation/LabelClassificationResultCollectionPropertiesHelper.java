// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.LabelClassificationResultCollection;

/**
 * The helper class to set the non-public properties of an {@link LabelClassificationResultCollection} instance.
 */
public final class LabelClassificationResultCollectionPropertiesHelper {
    private static LabelClassificationResultCollectionAccessor accessor;

    private LabelClassificationResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link LabelClassificationResultCollection}
     * instance.
     */
    public interface LabelClassificationResultCollectionAccessor {
        void setProjectName(LabelClassificationResultCollection resultCollection, String projectName);
        void setDeploymentName(LabelClassificationResultCollection resultCollection, String deploymentName);
        void setStatistics(LabelClassificationResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link LabelClassificationResultCollection} to set it's accessor.
     *
     * @param labelClassificationResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final LabelClassificationResultCollectionAccessor labelClassificationResultCollectionAccessor) {
        accessor = labelClassificationResultCollectionAccessor;
    }

    public static void setProjectName(LabelClassificationResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(LabelClassificationResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(LabelClassificationResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
