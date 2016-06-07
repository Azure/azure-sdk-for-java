/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A range of task ids that a task can depend on. All tasks with ids in the
 * range must complete successfully before the dependent task can be
 * scheduled.
 */
public class TaskIdRange {
    /**
     * The first task id in the range.
     */
    @JsonProperty(required = true)
    private int start;

    /**
     * The last task id in the range.
     */
    @JsonProperty(required = true)
    private int end;

    /**
     * Get the start value.
     *
     * @return the start value
     */
    public int start() {
        return this.start;
    }

    /**
     * Set the start value.
     *
     * @param start the start value to set
     * @return the TaskIdRange object itself.
     */
    public TaskIdRange withStart(int start) {
        this.start = start;
        return this;
    }

    /**
     * Get the end value.
     *
     * @return the end value
     */
    public int end() {
        return this.end;
    }

    /**
     * Set the end value.
     *
     * @param end the end value to set
     * @return the TaskIdRange object itself.
     */
    public TaskIdRange withEnd(int end) {
        this.end = end;
        return this;
    }

}
