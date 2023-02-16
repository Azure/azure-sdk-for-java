// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.implementation.ExtractSummaryResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.models.ExtractSummaryResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * A collection model that contains a list of {@link ExtractSummaryResult} along with model version and
 * batch's statistics.
 */
@Immutable
public final class ExtractSummaryResultCollection extends IterableStream<ExtractSummaryResult> {
    private String modelVersion;
    private TextDocumentBatchStatistics statistics;

    static {
        ExtractSummaryResultCollectionPropertiesHelper.setAccessor(
            new ExtractSummaryResultCollectionPropertiesHelper.ExtractSummaryResultCollectionAccessor() {
                @Override
                public void setModelVersion(ExtractSummaryResultCollection resultCollection,
                    String modelVersion) {
                    resultCollection.setModelVersion(modelVersion);
                }

                @Override
                public void setStatistics(ExtractSummaryResultCollection resultCollection,
                    TextDocumentBatchStatistics statistics) {
                    resultCollection.setStatistics(statistics);
                }
            });
    }

    /**
     * Create a {@link ExtractSummaryResultCollection} model that maintains a list of
     * {@link ExtractSummaryResult} along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link ExtractSummaryResult}.
     */
    public ExtractSummaryResultCollection(Iterable<ExtractSummaryResult> documentResults) {
        super(documentResults);
    }

    /**
     * Get the model version trained in service for the request.
     *
     * @return The model version trained in service for the request.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Get the batch statistics of response.
     *
     * @return The batch statistics of response.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return statistics;
    }

    private void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    private void setStatistics(TextDocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }
}
