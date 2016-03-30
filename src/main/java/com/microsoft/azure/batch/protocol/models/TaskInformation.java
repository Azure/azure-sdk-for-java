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
     * Gets or sets the URL of the task.
     */
    private String taskUrl;

    /**
     * Gets or sets the id of the job to which the task belongs.
     */
    private String jobId;

    /**
     * Gets or sets the id of the task.
     */
    private String taskId;

    /**
     * Gets or sets the id of the subtask if the task is a multi-instance task.
     */
    private Integer subtaskId;

    /**
     * Gets or sets the current state of the task. Possible values include:
     * 'active', 'preparing', 'running', 'completed'.
     */
    @JsonProperty(required = true)
    private TaskState taskState;

    /**
     * Gets or sets information about the execution of the task.
     */
    private TaskExecutionInformation executionInfo;

    /**
     * Get the taskUrl value.
     *
     * @return the taskUrl value
     */
    public String getTaskUrl() {
        return this.taskUrl;
    }

    /**
     * Set the taskUrl value.
     *
     * @param taskUrl the taskUrl value to set
     */
    public void setTaskUrl(String taskUrl) {
        this.taskUrl = taskUrl;
    }

    /**
     * Get the jobId value.
     *
     * @return the jobId value
     */
    public String getJobId() {
        return this.jobId;
    }

    /**
     * Set the jobId value.
     *
     * @param jobId the jobId value to set
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Get the taskId value.
     *
     * @return the taskId value
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * Set the taskId value.
     *
     * @param taskId the taskId value to set
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * Get the subtaskId value.
     *
     * @return the subtaskId value
     */
    public Integer getSubtaskId() {
        return this.subtaskId;
    }

    /**
     * Set the subtaskId value.
     *
     * @param subtaskId the subtaskId value to set
     */
    public void setSubtaskId(Integer subtaskId) {
        this.subtaskId = subtaskId;
    }

    /**
     * Get the taskState value.
     *
     * @return the taskState value
     */
    public TaskState getTaskState() {
        return this.taskState;
    }

    /**
     * Set the taskState value.
     *
     * @param taskState the taskState value to set
     */
    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }

    /**
     * Get the executionInfo value.
     *
     * @return the executionInfo value
     */
    public TaskExecutionInformation getExecutionInfo() {
        return this.executionInfo;
    }

    /**
     * Set the executionInfo value.
     *
     * @param executionInfo the executionInfo value to set
     */
    public void setExecutionInfo(TaskExecutionInformation executionInfo) {
        this.executionInfo = executionInfo;
    }

}
