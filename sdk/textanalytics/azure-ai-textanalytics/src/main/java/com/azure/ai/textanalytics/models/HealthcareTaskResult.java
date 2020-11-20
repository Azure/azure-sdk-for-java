// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareTaskResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizeHealthcareEntitiesResultCollection;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The HealthcareTaskResult model.
 */
public final class HealthcareTaskResult extends JobMetadata {
    private RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResults;
    private List<TextAnalyticsError> errors;

    static {
        HealthcareTaskResultPropertiesHelper.setAccessor(
            new HealthcareTaskResultPropertiesHelper.HealthcareTaskResultAccessor() {
                @Override
                public void setResult(HealthcareTaskResult healthcareTaskResult,
                    RecognizeHealthcareEntitiesResultCollection recognizeHealthcareEntitiesResultCollection) {
                    healthcareTaskResult.setResult(recognizeHealthcareEntitiesResultCollection);
                }

                @Override
                public void setErrors(HealthcareTaskResult healthcareTaskResult, List<TextAnalyticsError> errors) {
                    healthcareTaskResult.setErrors(errors);
                }
            });
    }

    /**
     * Creates a {@link HealthcareTaskResult} model that describes a healthcare task job result.
     *
     * @param jobId the job identification.
     * @param createdDateTime the created time of the job.
     * @param lastUpdateDateTime the last updated time of the job.
     * @param status the job status.
     * @param displayName the display name.
     * @param expirationDateTime the expiration time of the job.
     */
    public HealthcareTaskResult(String jobId, OffsetDateTime createdDateTime, OffsetDateTime lastUpdateDateTime,
        JobState status, String displayName, OffsetDateTime expirationDateTime) {
        super(jobId, createdDateTime, lastUpdateDateTime, status, displayName, expirationDateTime);
    }

    /**
     * Get a collection model that contains a list of {@link RecognizeHealthcareEntitiesResult} along with
     * model version and batch's statistics.
     *
     * @return the {@link RecognizeHealthcareEntitiesResultCollection},
     */
    public RecognizeHealthcareEntitiesResultCollection getResult() {
        return this.healthcareEntitiesResults;
    }

    /**
     * The private setter to set the healthcareEntitiesResults property
     * via {@link HealthcareTaskResultPropertiesHelper.HealthcareTaskResultAccessor}.
     *
     * @param healthcareEntitiesResults a collection model that contains a list of
     * {@link RecognizeHealthcareEntitiesResult} along with model version and batch's statistics.
     */
    private void setResult(RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResults) {
        this.healthcareEntitiesResults = healthcareEntitiesResults;
    }

    /**
     * Get a list of {@link TextAnalyticsError} for Healthcare tasks if operation failed.
     *
     * @return a list of {@link TextAnalyticsError}.
     */
    public List<TextAnalyticsError> getErrors() {
        return this.errors;
    }

    /**
     * The private setter to set the healthcareEntitiesResults property
     * via {@link HealthcareTaskResultPropertiesHelper.HealthcareTaskResultAccessor}.
     *
     * @param errors a list of {@link TextAnalyticsError} for Healthcare tasks.
     */
    private void setErrors(List<TextAnalyticsError> errors) {
        this.errors = errors;
    }
}
