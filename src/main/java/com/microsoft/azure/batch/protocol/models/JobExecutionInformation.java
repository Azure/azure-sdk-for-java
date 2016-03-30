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
     * Gets or sets the start time of the job.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * Gets or sets the completion time of the job. This property is set only
     * if the job is in the completed state.
     */
    private DateTime endTime;

    /**
     * Gets or sets the id of the pool to which this job is assigned.
     */
    private String poolId;

    /**
     * Gets or sets details of any error encountered by the service in
     * starting the job.
     */
    private JobSchedulingError schedulingError;

    /**
     * Gets or sets a string describing the reason the job ended.
     */
    private String terminateReason;

    /**
     * Get the startTime value.
     *
     * @return the startTime value
     */
    public DateTime getStartTime() {
        return this.startTime;
    }

    /**
     * Set the startTime value.
     *
     * @param startTime the startTime value to set
     */
    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Get the endTime value.
     *
     * @return the endTime value
     */
    public DateTime getEndTime() {
        return this.endTime;
    }

    /**
     * Set the endTime value.
     *
     * @param endTime the endTime value to set
     */
    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Get the poolId value.
     *
     * @return the poolId value
     */
    public String getPoolId() {
        return this.poolId;
    }

    /**
     * Set the poolId value.
     *
     * @param poolId the poolId value to set
     */
    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    /**
     * Get the schedulingError value.
     *
     * @return the schedulingError value
     */
    public JobSchedulingError getSchedulingError() {
        return this.schedulingError;
    }

    /**
     * Set the schedulingError value.
     *
     * @param schedulingError the schedulingError value to set
     */
    public void setSchedulingError(JobSchedulingError schedulingError) {
        this.schedulingError = schedulingError;
    }

    /**
     * Get the terminateReason value.
     *
     * @return the terminateReason value
     */
    public String getTerminateReason() {
        return this.terminateReason;
    }

    /**
     * Set the terminateReason value.
     *
     * @param terminateReason the terminateReason value to set
     */
    public void setTerminateReason(String terminateReason) {
        this.terminateReason = terminateReason;
    }

}
