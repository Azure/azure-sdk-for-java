// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link AnalyzeHealthcareEntitiesResultCollection} model.
 */
@Immutable
public final class AnalyzeHealthcareEntitiesResultCollection extends IterableStream<AnalyzeHealthcareEntitiesResult> {
    private TextDocumentBatchStatistics statistics;
    private String modelVersion;

    static {
        AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.setAccessor(
            new AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper
                    .AnalyzeHealthcareEntitiesResultCollectionAccessor() {
                @Override
                public void setModelVersion(
                    AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection,
                    String modelVersion) {
                    analyzeHealthcareEntitiesResultCollection.setModelVersion(modelVersion);
                }

                @Override
                public void setStatistics(
                    AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection,
                    TextDocumentBatchStatistics statistics) {
                    analyzeHealthcareEntitiesResultCollection.setStatistics(statistics);
                }
            });
    }

    /**
     * Creates a {@link AnalyzeHealthcareEntitiesResultCollection} model that maintains a list of
     * {@link AnalyzeHealthcareEntitiesResult} along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link AnalyzeHealthcareEntitiesResult}.
     */
    public AnalyzeHealthcareEntitiesResultCollection(Iterable<AnalyzeHealthcareEntitiesResult> documentResults) {
        super(documentResults);
    }

    /**
     * Gets the model version trained in service for the request.
     *
     * @return The model version trained in service for the request.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Gets the healthcare document result statistics properties.
     *
     * @return The healthcare document result statistics properties.
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
