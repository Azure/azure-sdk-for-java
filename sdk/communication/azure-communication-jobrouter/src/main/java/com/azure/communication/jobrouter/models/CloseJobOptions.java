// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.time.OffsetDateTime;

/**
 * Request options to close a job.
 * Job: A unit of work to be routed.
 */
public final class CloseJobOptions {

    /**
     * id of job to close.
     */
    private final String jobId;

    /**
     * id of assignment associated with the job.
     */
    private final String assignmentId;

    /**
     * Reason code for closing the job.
     */
    private String dispositionCode;

    /**
     * A future time determined to close the job.
     */
    private OffsetDateTime closeAt;

    /**
     * Note attached to job.
     */
    private String note;

    /**
     * Constructor for CloseJobOptions
     * @param jobId jobId to close.
     * @param assignmentId assignmentId associated with the job.
     */
    public CloseJobOptions(String jobId, String assignmentId) {
        this.jobId = jobId;
        this.assignmentId = assignmentId;
    }

    /**
     * Sets dispositionCode.
     * @param dispositionCode Reason code for cancelled or closed jobs.
     * @return object of type CloseJobOptions.
     */
    public CloseJobOptions setDispositionCode(String dispositionCode) {
        this.dispositionCode = dispositionCode;
        return this;
    }

    /**
     * Sets closeTime.
     * @param closeAt A future time determined to close the job.
     * @return object of type CloseJobOptions.
     */
    public CloseJobOptions setCloseAt(OffsetDateTime closeAt) {
        this.closeAt = closeAt;
        return this;
    }

    /**
     * Sets note.
     * @param note Note attached to job.
     * @return object of type CloseJobOptions
     */
    public CloseJobOptions setNote(String note) {
        this.note = note;
        return this;
    }

    /**
     * Get jobId.
     * @return jobId
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * Get assignmentId.
     * @return assignmentId
     */
    public String getAssignmentId() {
        return this.assignmentId;
    }

    /**
     * Get dispositionCode.
     * @return dispositionCode
     */
    public String getDispositionCode() {
        return this.dispositionCode;
    }

    /**
     * Gets closeAt.
     * @return closeAt
     */
    public OffsetDateTime getCloseAt() {
        return this.closeAt;
    }

    /**
     * Gets note.
     * @return note
     */
    public String getNote() {
        return this.note;
    }
}
