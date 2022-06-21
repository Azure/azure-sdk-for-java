// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.implementation.LabelClassificationResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.models.LabelClassificationResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * A collection model that contains a list of {@link LabelClassificationResult} along with project name,
 * deployment name and batch's statistics.
 */
@Immutable
public final class LabelClassificationResultCollection extends IterableStream<LabelClassificationResult> {
    private String projectName;
    private String deploymentName;
    private TextDocumentBatchStatistics statistics;

    static {
        LabelClassificationResultCollectionPropertiesHelper.setAccessor(
            new LabelClassificationResultCollectionPropertiesHelper.LabelClassificationResultCollectionAccessor() {
                @Override
                public void setProjectName(LabelClassificationResultCollection resultCollection,
                    String projectName) {
                    resultCollection.setProjectName(projectName);
                }

                @Override
                public void setDeploymentName(LabelClassificationResultCollection resultCollection,
                    String deploymentName) {
                    resultCollection.setDeploymentName(deploymentName);
                }

                @Override
                public void setStatistics(LabelClassificationResultCollection resultCollection,
                    TextDocumentBatchStatistics statistics) {
                    resultCollection.setStatistics(statistics);
                }
            });
    }

    /**
     * Create a {@link LabelClassificationResultCollection} model that maintains a list of
     * {@link LabelClassificationResult} along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link LabelClassificationResult}.
     */
    public LabelClassificationResultCollection(Iterable<LabelClassificationResult> documentResults) {
        super(documentResults);
    }

    /**
     * Gets the name of the project which owns the model being consumed.
     *
     * @return The name of the project which owns the model being consumed.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets the name of the deployment being consumed.
     *
     * @return The name of the deployment being consumed.
     */
    public String getDeploymentName() {
        return deploymentName;
    }

    /**
     * Get the batch statistics of response.
     *
     * @return The batch statistics of response.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return statistics;
    }

    private void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    private void setStatistics(TextDocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }
}
