// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.ai.textanalytics.util.LabelClassifyResultCollection;

/**
 * The helper class to set the non-public properties of an {@link LabelClassifyResultCollection} instance.
 */
public final class LabelClassifyResultCollectionPropertiesHelper {
    private static LabelClassifyResultCollectionAccessor accessor;

    private LabelClassifyResultCollectionPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link LabelClassifyResultCollection}
     * instance.
     */
    public interface LabelClassifyResultCollectionAccessor {
        void setProjectName(LabelClassifyResultCollection resultCollection, String projectName);
        void setDeploymentName(LabelClassifyResultCollection resultCollection, String deploymentName);
        void setStatistics(LabelClassifyResultCollection resultCollection,
            TextDocumentBatchStatistics statistics);
    }

    /**
     * The method called from {@link LabelClassifyResultCollection} to set it's accessor.
     *
     * @param labelClassifyResultCollectionAccessor The accessor.
     */
    public static void setAccessor(
        final LabelClassifyResultCollectionAccessor labelClassifyResultCollectionAccessor) {
        accessor = labelClassifyResultCollectionAccessor;
    }

    public static void setProjectName(LabelClassifyResultCollection resultCollection, String projectName) {
        accessor.setProjectName(resultCollection, projectName);
    }

    public static void setDeploymentName(LabelClassifyResultCollection resultCollection,
        String deploymentName) {
        accessor.setDeploymentName(resultCollection, deploymentName);
    }

    public static void setStatistics(LabelClassifyResultCollection resultCollection,
        TextDocumentBatchStatistics statistics) {
        accessor.setStatistics(resultCollection, statistics);
    }
}
