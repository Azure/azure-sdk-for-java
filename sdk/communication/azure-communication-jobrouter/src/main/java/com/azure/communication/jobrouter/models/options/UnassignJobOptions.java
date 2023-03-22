// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

public final class UnassignJobOptions {
    /**
     * id of job to close.
     */
    private final String jobId;

    /**
     * id of assignment associated with the job.
     */
    private final String assignmentId;

    /**
     * If set to true, the service will not re-match the job to a worker.
     */
    private Boolean isWorkerRematchPending;

    /**
     * Constructor for UnassignJobOptions.
     * @param jobId jobId to unassign.
     * @param assignmentId assignmentId of the job.
     */
    public UnassignJobOptions(String jobId, String assignmentId) {
        this.jobId = jobId;
        this.assignmentId = assignmentId;
    }

    /**
     * Sets isWorkerRematchPending to true or false.
     * If set to true, the job is NOT queued for re-matching.
     * @param isWorkerRematchPending
     */
    public void setIsWorkerRematchPending(Boolean isWorkerRematchPending) {
        this.isWorkerRematchPending = isWorkerRematchPending;
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
}
