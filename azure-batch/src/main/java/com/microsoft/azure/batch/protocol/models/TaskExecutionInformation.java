/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about the execution of a task.
 */
public class TaskExecutionInformation {
    /**
     * The time at which the task started running. If the task has been
     * restarted or retried, this is the most recent time at which the task
     * started running.
     */
    private DateTime startTime;

    /**
     * The time at which the task completed. This property is set only if the
     * task is in the Completed state.
     */
    private DateTime endTime;

    /**
     * The exit code of the task. This property is set only if the task is in
     * completed state.
     */
    private Integer exitCode;

    /**
     * Details of any error encountered scheduling the task.
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
     * The number of times the task has been requeued by the Batch service as
     * the result of a user request.
     */
    @JsonProperty(required = true)
    private int requeueCount;

    /**
     * The most recent time at which the task has been requeued by the Batch
     * service as the result of a user request.
     */
    private DateTime lastRequeueTime;

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
     * @return the TaskExecutionInformation object itself.
     */
    public TaskExecutionInformation withStartTime(DateTime startTime) {
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
     * @return the TaskExecutionInformation object itself.
     */
    public TaskExecutionInformation withEndTime(DateTime endTime) {
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
     * @return the TaskExecutionInformation object itself.
     */
    public TaskExecutionInformation withExitCode(Integer exitCode) {
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
     * @return the TaskExecutionInformation object itself.
     */
    public TaskExecutionInformation withSchedulingError(TaskSchedulingError schedulingError) {
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
     * @return the TaskExecutionInformation object itself.
     */
    public TaskExecutionInformation withRetryCount(int retryCount) {
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
     * @return the TaskExecutionInformation object itself.
     */
    public TaskExecutionInformation withLastRetryTime(DateTime lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
        return this;
    }

    /**
     * Get the requeueCount value.
     *
     * @return the requeueCount value
     */
    public int requeueCount() {
        return this.requeueCount;
    }

    /**
     * Set the requeueCount value.
     *
     * @param requeueCount the requeueCount value to set
     * @return the TaskExecutionInformation object itself.
     */
    public TaskExecutionInformation withRequeueCount(int requeueCount) {
        this.requeueCount = requeueCount;
        return this;
    }

    /**
     * Get the lastRequeueTime value.
     *
     * @return the lastRequeueTime value
     */
    public DateTime lastRequeueTime() {
        return this.lastRequeueTime;
    }

    /**
     * Set the lastRequeueTime value.
     *
     * @param lastRequeueTime the lastRequeueTime value to set
     * @return the TaskExecutionInformation object itself.
     */
    public TaskExecutionInformation withLastRequeueTime(DateTime lastRequeueTime) {
        this.lastRequeueTime = lastRequeueTime;
        return this;
    }

}
