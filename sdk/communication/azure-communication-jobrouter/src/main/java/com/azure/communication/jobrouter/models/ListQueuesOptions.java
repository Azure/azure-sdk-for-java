// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

/**
 * Request options to list queues.
 * Queue: A queue that can contain jobs to be routed.
 */
@Fluent
public class ListQueuesOptions {
    /**
     * Maximum number of items per page.
     */
    private final Integer maxPageSize;

    /**
     * Constructor for ListQueuesOptions.
     * @param maxPageSize Maximum number of items per page.
     */
    public ListQueuesOptions(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    /**
     * Returns maxPageSize.
     * @return maxPageSize
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }
}
