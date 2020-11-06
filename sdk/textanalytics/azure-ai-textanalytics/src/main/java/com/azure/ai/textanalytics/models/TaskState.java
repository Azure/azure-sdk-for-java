// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * The TaskState model.
 */
@Fluent
public class TaskState {
    /*
     * The lastUpdateDateTime property.
     */
    private OffsetDateTime lastUpdateDateTime;

    /*
     * The name property.
     */
    private String name;

    /*
     * The status property.
     */
    private JobState status;

    /**
     * Get the lastUpdateDateTime property: The lastUpdateDateTime property.
     *
     * @return the lastUpdateDateTime value.
     */
    public OffsetDateTime getLastUpdateDateTime() {
        return this.lastUpdateDateTime;
    }

    /**
     * Set the lastUpdateDateTime property: The lastUpdateDateTime property.
     *
     * @param lastUpdateDateTime the lastUpdateDateTime value to set.
     * @return the TaskState object itself.
     */
    public TaskState setLastUpdateDateTime(OffsetDateTime lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
        return this;
    }

    /**
     * Get the name property: The name property.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The name property.
     *
     * @param name the name value to set.
     * @return the TaskState object itself.
     */
    public TaskState setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the status property: The status property.
     *
     * @return the status value.
     */
    public JobState getStatus() {
        return this.status;
    }

    /**
     * Set the status property: The status property.
     *
     * @param status the status value to set.
     * @return the TaskState object itself.
     */
    public TaskState setStatus(JobState status) {
        this.status = status;
        return this;
    }
}
