/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The TaskIdRange model.
 */
public class TaskIdRange {
    /**
     * Gets or sets the first task id in the range.
     */
    @JsonProperty(required = true)
    private int start;

    /**
     * Gets or sets the last task id in the range.
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
    public TaskIdRange setStart(int start) {
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
    public TaskIdRange setEnd(int end) {
        this.end = end;
        return this;
    }

}
