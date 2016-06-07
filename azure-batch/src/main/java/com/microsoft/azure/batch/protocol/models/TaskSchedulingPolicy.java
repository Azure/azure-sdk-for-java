/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specifies how tasks should be distributed across compute nodes.
 */
public class TaskSchedulingPolicy {
    /**
     * How tasks should be distributed across compute nodes. Possible values
     * include: 'spread', 'pack', 'unmapped'.
     */
    @JsonProperty(required = true)
    private ComputeNodeFillType nodeFillType;

    /**
     * Get the nodeFillType value.
     *
     * @return the nodeFillType value
     */
    public ComputeNodeFillType nodeFillType() {
        return this.nodeFillType;
    }

    /**
     * Set the nodeFillType value.
     *
     * @param nodeFillType the nodeFillType value to set
     * @return the TaskSchedulingPolicy object itself.
     */
    public TaskSchedulingPolicy withNodeFillType(ComputeNodeFillType nodeFillType) {
        this.nodeFillType = nodeFillType;
        return this;
    }

}
