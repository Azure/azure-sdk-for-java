/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contains information about the execution of a Job Release task on a compute
 * node.
 */
public class JobReleaseTaskExecutionInformation {
    /**
     * The time at which the task started running. Note that every time the
     * task is restarted, this value is updated.
     */
    @JsonProperty(required = true)
    private DateTime startTime;

    /**
     * The time at which the Job Release task completed. This property is set
     * only if the task is in the Completed state.
     */
    private DateTime endTime;

    /**
     * The current state of the Job Release task. Possible values include:
     * 'running', 'completed'.
     */
    @JsonProperty(required = true)
    private JobReleaseTaskState state;

    /**
     * The root directory of the Job Release task on the compute node. You can
     * use this path to retrieve files created by the task, such as log files.
     */
    private String taskRootDirectory;

    /**
     * The URL to the root directory of the Job Release task on the compute
     * node.
     */
    private String taskRootDirectoryUrl;

    /**
     * The exit code of the Job Release task. This property is set only if the
     * task is in the Completed state.
     */
    private Integer exitCode;

    /**
     * The scheduling error encountered by the Batch service when starting the
     * task.
     */
    private TaskSchedulingError schedulingError;

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
     * @return the JobReleaseTaskExecutionInformation object itself.
     */
    public JobReleaseTaskExecutionInformation withStartTime(DateTime startTime) {
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
     * @return the JobReleaseTaskExecutionInformation object itself.
     */
    public JobReleaseTaskExecutionInformation withEndTime(DateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public JobReleaseTaskState state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the JobReleaseTaskExecutionInformation object itself.
     */
    public JobReleaseTaskExecutionInformation withState(JobReleaseTaskState state) {
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
     * @return the JobReleaseTaskExecutionInformation object itself.
     */
    public JobReleaseTaskExecutionInformation withTaskRootDirectory(String taskRootDirectory) {
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
     * @return the JobReleaseTaskExecutionInformation object itself.
     */
    public JobReleaseTaskExecutionInformation withTaskRootDirectoryUrl(String taskRootDirectoryUrl) {
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
     * @return the JobReleaseTaskExecutionInformation object itself.
     */
    public JobReleaseTaskExecutionInformation withExitCode(Integer exitCode) {
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
     * @return the JobReleaseTaskExecutionInformation object itself.
     */
    public JobReleaseTaskExecutionInformation withSchedulingError(TaskSchedulingError schedulingError) {
        this.schedulingError = schedulingError;
        return this;
    }

}
