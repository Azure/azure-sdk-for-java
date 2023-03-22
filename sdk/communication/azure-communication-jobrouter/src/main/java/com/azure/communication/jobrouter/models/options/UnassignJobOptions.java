package com.azure.communication.jobrouter.models.options;

import java.time.OffsetDateTime;

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

    public UnassignJobOptions(String jobId, String assignmentId) {
        this.jobId = jobId;
        this.assignmentId = assignmentId;
    }

    public void setSetToPending(Boolean isWorkerRematchPending) {
        this.isWorkerRematchPending = isWorkerRematchPending;
    }


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
