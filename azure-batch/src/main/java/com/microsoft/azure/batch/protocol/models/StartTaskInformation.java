/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about a start task running on a compute node.
 */
public class StartTaskInformation {
    /**
     * The state of the start task on the compute node. Possible values
     * include: 'running', 'completed'.
     */
    @JsonProperty(required = true)
    private StartTaskState state;

    /**
     * The time at which the start task started running.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * The time at which the start task stopped running.
     */
    private DateTime endTime;

    /**
     * The exit code of the start task.
     */
    private Integer exitCode;

    /**
     * Any error encountered scheduling the start task.
     */
    private TaskSchedulingError schedulingError;

    /**
     * The number of times the task has been retried by the Batch service.
     */
    @JsonProperty(required = true)
    private int retryCount;

    /**
     * The most recent time at which a retry of the task started running.
     */
    private DateTime lastRetryTime;

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public StartTaskState state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the StartTaskInformation object itself.
     */
    public StartTaskInformation withState(StartTaskState state) {
        this.state = state;
        return this;
    }

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
     * @return the StartTaskInformation object itself.
     */
    public StartTaskInformation withStartTime(DateTime startTime) {
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
     * @return the StartTaskInformation object itself.
     */
    public StartTaskInformation withEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the exitCode value.
     *
     * @return the exitCode value
     */
    public Integer exitCode() {
        return this.exitCode;
    }

    /**
     * Set the exitCode value.
     *
     * @param exitCode the exitCode value to set
     * @return the StartTaskInformation object itself.
     */
    public StartTaskInformation withExitCode(Integer exitCode) {
        this.exitCode = exitCode;
        return this;
    }

    /**
     * Get the schedulingError value.
     *
     * @return the schedulingError value
     */
    public TaskSchedulingError schedulingError() {
        return this.schedulingError;
    }

    /**
     * Set the schedulingError value.
     *
     * @param schedulingError the schedulingError value to set
     * @return the StartTaskInformation object itself.
     */
    public StartTaskInformation withSchedulingError(TaskSchedulingError schedulingError) {
        this.schedulingError = schedulingError;
        return this;
    }

    /**
     * Get the retryCount value.
     *
     * @return the retryCount value
     */
    public int retryCount() {
        return this.retryCount;
    }

    /**
     * Set the retryCount value.
     *
     * @param retryCount the retryCount value to set
     * @return the StartTaskInformation object itself.
     */
    public StartTaskInformation withRetryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    /**
     * Get the lastRetryTime value.
     *
     * @return the lastRetryTime value
     */
    public DateTime lastRetryTime() {
        return this.lastRetryTime;
    }

    /**
     * Set the lastRetryTime value.
     *
     * @param lastRetryTime the lastRetryTime value to set
     * @return the StartTaskInformation object itself.
     */
    public StartTaskInformation withLastRetryTime(DateTime lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
        return this;
    }

}
