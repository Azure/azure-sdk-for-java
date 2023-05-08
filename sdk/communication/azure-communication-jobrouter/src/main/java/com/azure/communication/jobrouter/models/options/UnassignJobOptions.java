// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

/**
 * Request options to unassign a job from a worker.
 */
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
     * Constructor for UnassignJobOptions.
     * @param jobId jobId to unassign.
     * @param assignmentId assignmentId of the job.
     */
    public UnassignJobOptions(String jobId, String assignmentId) {
        this.jobId = jobId;
        this.assignmentId = assignmentId;
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
