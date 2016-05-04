/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains information about the execution of a Job Preparation task on a
 * compute node.
 */
public class JobPreparationTaskExecutionInformation {
    /**
     * Gets or sets the time at which the Job Preparation task started running.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * Gets or sets the time at which the Job Preparation task completed. This
     * property is set only if the task is in the Completed state.
     */
    private DateTime endTime;

    /**
     * Gets or sets the current running state of the Job Preparation task on
     * the compute node. Possible values include: 'running', 'completed'.
     */
    @JsonProperty(required = true)
    private JobPreparationTaskState state;

    /**
     * Gets or sets the root directory of the Job Preparation task on the
     * compute node.
     */
    private String taskRootDirectory;

    /**
     * Gets or sets the URL to the root directory of the Job Preparation task
     * on the compute node.
     */
    private String taskRootDirectoryUrl;

    /**
     * Gets or sets the exit code of the Job Preparation task. This property
     * is set only if the task is in the Completed state.
     */
    private Integer exitCode;

    /**
     * Gets or sets any error starting the Job Preparation task.
     */
    private TaskSchedulingError schedulingError;

    /**
     * Gets or sets the number of times the Job Preparation task has been
     * retried by the Batch service.
     */
    @JsonProperty(required = true)
    private int retryCount;

    /**
     * Gets or sets the most recent time at which a retry of the Job
     * Preparation task started running. This property is set only if the
     * task was retried (i.e. retryCount is nonzero).
     */
    private DateTime lastRetryTime;

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
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setStartTime(DateTime startTime) {
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
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public JobPreparationTaskState state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setState(JobPreparationTaskState state) {
        this.state = state;
        return this;
    }

    /**
     * Get the taskRootDirectory value.
     *
     * @return the taskRootDirectory value
     */
    public String taskRootDirectory() {
        return this.taskRootDirectory;
    }

    /**
     * Set the taskRootDirectory value.
     *
     * @param taskRootDirectory the taskRootDirectory value to set
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setTaskRootDirectory(String taskRootDirectory) {
        this.taskRootDirectory = taskRootDirectory;
        return this;
    }

    /**
     * Get the taskRootDirectoryUrl value.
     *
     * @return the taskRootDirectoryUrl value
     */
    public String taskRootDirectoryUrl() {
        return this.taskRootDirectoryUrl;
    }

    /**
     * Set the taskRootDirectoryUrl value.
     *
     * @param taskRootDirectoryUrl the taskRootDirectoryUrl value to set
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setTaskRootDirectoryUrl(String taskRootDirectoryUrl) {
        this.taskRootDirectoryUrl = taskRootDirectoryUrl;
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
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setExitCode(Integer exitCode) {
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
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setSchedulingError(TaskSchedulingError schedulingError) {
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
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setRetryCount(int retryCount) {
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
     * @return the JobPreparationTaskExecutionInformation object itself.
     */
    public JobPreparationTaskExecutionInformation setLastRetryTime(DateTime lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
        return this;
    }

}
