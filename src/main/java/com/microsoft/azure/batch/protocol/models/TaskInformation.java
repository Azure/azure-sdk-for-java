/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about a task running on a compute node.
 */
public class TaskInformation {
    /**
     * The URL of the task.
     */
    private String taskUrl;

    /**
     * The id of the job to which the task belongs.
     */
    private String jobId;

    /**
     * The id of the task.
     */
    private String taskId;

    /**
     * The id of the subtask if the task is a multi-instance task.
     */
    private Integer subtaskId;

    /**
     * The current state of the task. Possible values include: 'active',
     * 'preparing', 'running', 'completed'.
     */
    @JsonProperty(required = true)
    private TaskState taskState;

    /**
     * Information about the execution of the task.
     */
    private TaskExecutionInformation executionInfo;

    /**
     * Get the taskUrl value.
     *
     * @return the taskUrl value
     */
    public String taskUrl() {
        return this.taskUrl;
    }

    /**
     * Set the taskUrl value.
     *
     * @param taskUrl the taskUrl value to set
     * @return the TaskInformation object itself.
     */
    public TaskInformation withTaskUrl(String taskUrl) {
        this.taskUrl = taskUrl;
        return this;
    }

    /**
     * Get the jobId value.
     *
     * @return the jobId value
     */
    public String jobId() {
        return this.jobId;
    }

    /**
     * Set the jobId value.
     *
     * @param jobId the jobId value to set
     * @return the TaskInformation object itself.
     */
    public TaskInformation withJobId(String jobId) {
        this.jobId = jobId;
        return this;
    }

    /**
     * Get the taskId value.
     *
     * @return the taskId value
     */
    public String taskId() {
        return this.taskId;
    }

    /**
     * Set the taskId value.
     *
     * @param taskId the taskId value to set
     * @return the TaskInformation object itself.
     */
    public TaskInformation withTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    /**
     * Get the subtaskId value.
     *
     * @return the subtaskId value
     */
    public Integer subtaskId() {
        return this.subtaskId;
    }

    /**
     * Set the subtaskId value.
     *
     * @param subtaskId the subtaskId value to set
     * @return the TaskInformation object itself.
     */
    public TaskInformation withSubtaskId(Integer subtaskId) {
        this.subtaskId = subtaskId;
        return this;
    }

    /**
     * Get the taskState value.
     *
     * @return the taskState value
     */
    public TaskState taskState() {
        return this.taskState;
    }

    /**
     * Set the taskState value.
     *
     * @param taskState the taskState value to set
     * @return the TaskInformation object itself.
     */
    public TaskInformation withTaskState(TaskState taskState) {
        this.taskState = taskState;
        return this;
    }

    /**
     * Get the executionInfo value.
     *
     * @return the executionInfo value
     */
    public TaskExecutionInformation executionInfo() {
        return this.executionInfo;
    }

    /**
     * Set the executionInfo value.
     *
     * @param executionInfo the executionInfo value to set
     * @return the TaskInformation object itself.
     */
    public TaskInformation withExecutionInfo(TaskExecutionInformation executionInfo) {
        this.executionInfo = executionInfo;
        return this;
    }

}
