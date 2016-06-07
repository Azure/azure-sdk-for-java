/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains information about the execution of a job in the Azure Batch
 * service.
 */
public class JobExecutionInformation {
    /**
     * The start time of the job.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * The completion time of the job. This property is set only if the job is
     * in the completed state.
     */
    private DateTime endTime;

    /**
     * The id of the pool to which this job is assigned.
     */
    private String poolId;

    /**
     * Details of any error encountered by the service in starting the job.
     */
    private JobSchedulingError schedulingError;

    /**
     * A string describing the reason the job ended.
     */
    private String terminateReason;

    /**
     * Get the startTime value.
     *
     * @return the startTime value
     */
    public DateTime startTime() {
        return this.startTime;
    }

    /**
     * Set the startTime value.
     *
     * @param startTime the startTime value to set
     * @return the JobExecutionInformation object itself.
     */
    public JobExecutionInformation withStartTime(DateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Get the endTime value.
     *
     * @return the endTime value
     */
    public DateTime endTime() {
        return this.endTime;
    }

    /**
     * Set the endTime value.
     *
     * @param endTime the endTime value to set
     * @return the JobExecutionInformation object itself.
     */
    public JobExecutionInformation withEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the poolId value.
     *
     * @return the poolId value
     */
    public String poolId() {
        return this.poolId;
    }

    /**
     * Set the poolId value.
     *
     * @param poolId the poolId value to set
     * @return the JobExecutionInformation object itself.
     */
    public JobExecutionInformation withPoolId(String poolId) {
        this.poolId = poolId;
        return this;
    }

    /**
     * Get the schedulingError value.
     *
     * @return the schedulingError value
     */
    public JobSchedulingError schedulingError() {
        return this.schedulingError;
    }

    /**
     * Set the schedulingError value.
     *
     * @param schedulingError the schedulingError value to set
     * @return the JobExecutionInformation object itself.
     */
    public JobExecutionInformation withSchedulingError(JobSchedulingError schedulingError) {
        this.schedulingError = schedulingError;
        return this;
    }

    /**
     * Get the terminateReason value.
     *
     * @return the terminateReason value
     */
    public String terminateReason() {
        return this.terminateReason;
    }

    /**
     * Set the terminateReason value.
     *
     * @param terminateReason the terminateReason value to set
     * @return the JobExecutionInformation object itself.
     */
    public JobExecutionInformation withTerminateReason(String terminateReason) {
        this.terminateReason = terminateReason;
        return this;
    }

}
