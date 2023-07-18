// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

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

    /*
     * If suspendMatching is true, then the job is not queued for re-matching with a worker.
     */
    private Boolean suspendMatching;

    /**
     * Get the suspendMatching property: If suspendMatching is true, then the job is not queued for re-matching with a
     * worker.
     *
     * @return the suspendMatching value.
     */
    public Boolean isSuspendMatching() {
        return this.suspendMatching;
    }

    /**
     * Set the suspendMatching property: If suspendMatching is true, then the job is not queued for re-matching with a
     * worker.
     *
     * @param suspendMatching the suspendMatching value to set.
     * @return the UnassignJobOptions object itself.
     */
    public UnassignJobOptions setSuspendMatching(Boolean suspendMatching) {
        this.suspendMatching = suspendMatching;
        return this;
    }

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
