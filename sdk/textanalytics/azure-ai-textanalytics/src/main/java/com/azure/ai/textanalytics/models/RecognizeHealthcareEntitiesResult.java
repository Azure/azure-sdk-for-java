// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.RecognizeHealthcareEntitiesResultPropertiesHelper;
import com.azure.core.util.IterableStream;

/**
 * The {@link RecognizeHealthcareEntitiesResult} model.
 */
public final class RecognizeHealthcareEntitiesResult extends TextAnalyticsResult {
    private HealthcareEntityCollection healthcareEntityCollection;

    static {
        RecognizeHealthcareEntitiesResultPropertiesHelper.setAccessor(
            RecognizeHealthcareEntitiesResult::setEntities);
    }

    /**
     * Creates a {@link RecognizeHealthcareEntitiesResult} model that describes recognized healthcare entities result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public RecognizeHealthcareEntitiesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Get an {@link IterableStream} of {@link HealthcareEntity}.
     *
     * @return An {@link IterableStream} of {@link HealthcareEntity}.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public HealthcareEntityCollection getEntities() {
        throwExceptionIfError();
        return healthcareEntityCollection;
    }

    /**
     * The private setter to set the healthcareEntityCollection property
     * via {@link RecognizeHealthcareEntitiesResultPropertiesHelper.RecognizeHealthcareEntitiesResultAccessor}.
     *
     * @param healthcareEntityCollection the {@link HealthcareEntityCollection}.
     */
    private void setEntities(HealthcareEntityCollection healthcareEntityCollection) {
        this.healthcareEntityCollection = healthcareEntityCollection;
    }
}
