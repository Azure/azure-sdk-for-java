// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import java.time.OffsetDateTime;

/**
 * The JobMetadata model
 */
public class JobMetadata {
    /*
     * The jobId property.
     */
    private final String jobId;

    /*
     * The createdDateTime property.
     */
    private final OffsetDateTime createdDateTime;

    /*
     * The displayName property.
     */
    private final String displayName;

    /*
     * The expirationDateTime property.
     */
    private final OffsetDateTime expirationDateTime;

    /*
     * The lastUpdateDateTime property.
     */
    private final OffsetDateTime lastUpdateDateTime;

    /*
     * The status property.
     */
    private final JobState status;

    /**
     * Creates a {@link JobMetadata} model that describes a task job's metadata.
     *
     * @param jobId the job identification.
     * @param createdDateTime the created time of the job.
     * @param lastUpdateDateTime the last updated time of the job.
     * @param status the job status.
     * @param displayName the display name.
     * @param expirationDateTime the expiration time of the job.
     */
    public JobMetadata(String jobId, OffsetDateTime createdDateTime, OffsetDateTime lastUpdateDateTime,
        JobState status, String displayName, OffsetDateTime expirationDateTime) {
        this.jobId = jobId;
        this.createdDateTime = createdDateTime;
        this.displayName = displayName;
        this.expirationDateTime = expirationDateTime;
        this.lastUpdateDateTime = lastUpdateDateTime;
        this.status = status;
    }

    /**
     * Get the jobId property: The jobId property.
     *
     * @return the jobId value.
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * Get the createdDateTime property: The createdDateTime property.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedDateTime() {
        return this.createdDateTime;
    }

    /**
     * Get the displayName property: The displayName property.
     *
     * @return the displayName value.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Get the expirationDateTime property: The expirationDateTime property.
     *
     * @return the expirationDateTime value.
     */
    public OffsetDateTime getExpirationDateTime() {
        return this.expirationDateTime;
    }

    /**
     * Get the lastUpdateDateTime property: The lastUpdateDateTime property.
     *
     * @return the lastUpdateDateTime value.
     */
    public OffsetDateTime getLastUpdateDateTime() {
        return this.lastUpdateDateTime;
    }

    /**
     * Get the status property: The status property.
     *
     * @return the status value.
     */
    public JobState getStatus() {
        return this.status;
    }
}
