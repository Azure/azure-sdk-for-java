// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.HealthcareTaskResultPropertiesHelper;
import com.azure.ai.textanalytics.util.RecognizeHealthcareEntitiesResultCollection;

import java.time.OffsetDateTime;

/**
 * The HealthcareTaskResult model.
 */
public final class HealthcareTaskResult extends JobMetadata {
    private RecognizeHealthcareEntitiesResultCollection healthcareEntitiesResults;

    static {
        HealthcareTaskResultPropertiesHelper.setAccessor(HealthcareTaskResult::setResult);
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
}
