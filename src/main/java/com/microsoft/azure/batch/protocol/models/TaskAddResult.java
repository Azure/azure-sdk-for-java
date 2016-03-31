/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result for a single task added as part of an add task collection operation.
 */
public class TaskAddResult {
    /**
     * The status of the add task request. Possible values include: 'success',
     * 'clienterror', 'servererror', 'unmapped'.
     */
    @JsonProperty(required = true)
    private TaskAddStatus status;

    /**
     * The id of the task for which this is the result.
     */
    @JsonProperty(required = true)
    private String taskId;

    /**
     * The ETag of the task, if the task was successfully added.
     */
    private String eTag;

    /**
     * The last modified time of the task.
     */
    private DateTime lastModified;

    /**
     * The URL of the task, if the task was successfully added.
     */
    private String location;

    /**
     * The error encountered while attempting to add the task.
     */
    private BatchError error;

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public TaskAddStatus getStatus() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     */
    public void setStatus(TaskAddStatus status) {
        this.status = status;
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
     * Get the eTag value.
     *
     * @return the eTag value
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag value.
     *
     * @param eTag the eTag value to set
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime getLastModified() {
        return this.lastModified;
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     */
    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Get the location value.
     *
     * @return the location value
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the location value to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Get the error value.
     *
     * @return the error value
     */
    public BatchError getError() {
        return this.error;
    }

    /**
     * Set the error value.
     *
     * @param error the error value to set
     */
    public void setError(BatchError error) {
        this.error = error;
    }

}
