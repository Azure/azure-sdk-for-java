// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.util;

import com.azure.ai.textanalytics.implementation.AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper;
import com.azure.ai.textanalytics.models.AnalyzeHealthcareEntitiesResult;
import com.azure.ai.textanalytics.models.TextAnalyticsError;
import com.azure.ai.textanalytics.models.TextDocumentBatchStatistics;
import com.azure.core.util.IterableStream;

/**
 * The HealthcareTaskResult model.
 */
public final class AnalyzeHealthcareEntitiesResultCollection extends IterableStream<AnalyzeHealthcareEntitiesResult> {
    private TextDocumentBatchStatistics statistics;
    private IterableStream<TextAnalyticsError> taskErrors;
    private String modelVersion;

    static {
        AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.setAccessor(
            new AnalyzeHealthcareEntitiesResultCollectionPropertiesHelper.AnalyzeHealthcareEntitiesResultCollectionAccessor() {
                @Override
                public void setModelVersion(AnalyzeHealthcareEntitiesResultCollection healthcareEntitiesResultCollection,
                    String modelVersion) {
                    healthcareEntitiesResultCollection.setModelVersion(modelVersion);
                }

                @Override
                public void setErrors(AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection,
                    IterableStream<TextAnalyticsError> taskErrors) {
                    analyzeHealthcareEntitiesResultCollection.setErrors(taskErrors);
                }

                @Override
                public void setStatistics(AnalyzeHealthcareEntitiesResultCollection analyzeHealthcareEntitiesResultCollection,
                    TextDocumentBatchStatistics statistics) {
                    analyzeHealthcareEntitiesResultCollection.setStatistics(statistics);
                }
            });
    }

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
     * @return the healthcare document result statistics properties.
     */
    public TextDocumentBatchStatistics getStatistics() {
        return statistics;
    }

    /**
     * Get an {@link IterableStream} of {@link TextAnalyticsError} for Healthcare tasks if operation failed.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsError}.
     */
    public IterableStream<TextAnalyticsError> getErrors() {
        return this.taskErrors;
    }

    private void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    private void setErrors(IterableStream<TextAnalyticsError> taskErrors) {
        this.taskErrors = taskErrors;
    }

    private void setStatistics(TextDocumentBatchStatistics statistics) {
        this.statistics = statistics;
    }
}
