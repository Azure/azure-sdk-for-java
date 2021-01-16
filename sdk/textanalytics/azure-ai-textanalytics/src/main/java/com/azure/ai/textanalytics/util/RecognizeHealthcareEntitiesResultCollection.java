// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.implementation.RecognizeHealthcareEntitiesResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.models.RecognizeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.util.IterableStream;

/**
 * A collection model that contains a list of {@link RecognizeHealthcareEntitiesResult} along with model version and
 * batch's statistics.
 */
public final class RecognizeHealthcareEntitiesResultCollection
    extends IterableStream<RecognizeHealthcareEntitiesResult> {
    private String modelVersion;
    private TextDocumentBatchStatistics statistics;

    static {
        RecognizeHealthcareEntitiesResultCollectionPropertiesHelper.setAccessor(
            new RecognizeHealthcareEntitiesResultCollectionPropertiesHelper
                .RecognizeHealthcareEntitiesResultCollectionAccessor() {
                @Override
                public void setModelVersion(
                    RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
                    String modelVersion) {
                    healthcareEntitiesResultCollection.setModelVersion(modelVersion);
                }

                @Override
                public void setStatistics(
                    RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
                    TextDocumentBatchStatistics statistics) {
                    healthcareEntitiesResultCollection.setStatistics(statistics);
                }
            });
    }

    /**
     * Create a {@link RecognizeHealthcareEntitiesResultCollection} model that maintains a list of
     * {@link RecognizeHealthcareEntitiesResult} along with model version and batch's statistics.
     *
     * @param documentResults A list of {@link RecognizeHealthcareEntitiesResult}.
     */
    public RecognizeHealthcareEntitiesResultCollection(Iterable<RecognizeHealthcareEntitiesResult> documentResults) {
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
