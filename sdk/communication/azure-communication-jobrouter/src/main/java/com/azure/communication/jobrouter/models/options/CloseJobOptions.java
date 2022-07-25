// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import java.time.OffsetDateTime;

/**
 * Request options to close a job.
 * Job: A unit of work to be routed.
 */
public class CloseJobOptions {

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
    private OffsetDateTime closeTime;

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
     */
    public void setDispositionCode(String dispositionCode) {
        this.dispositionCode = dispositionCode;
    }

    /**
     * Sets closeTime.
     * @param closeTime A future time determined to close the job.
     */
    public void setCloseTime(OffsetDateTime closeTime) {
        this.closeTime = closeTime;
    }

    /**
     * Sets note.
     * @param note Note attached to job.
     */
    public void setNote(String note) {
        this.note = note;
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
     * Gets closeTime.
     * @return closeTime
     */
    public OffsetDateTime getCloseTime() {
        return this.closeTime;
    }

    /**
     * Gets note.
     * @return note
     */
    public String getNote() {
        return this.note;
    }
}
