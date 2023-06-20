// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.communication.jobrouter.models.WorkerStateSelector;

/**
 * Request options to list workers.
 * Worker: An entity for jobs to be routed to.
 */
public class ListWorkersOptions {

    /**
     * workerStateSelector.
     */
    private final WorkerStateSelector workerStateSelector;

    /**
     * channelId.
     */
    private final String channelId;

    /**
     * queueId.
     */
    private final String queueId;

    /**
     * hasCapacity.
     */
    private final Boolean hasCapacity;

    /**
     * maxPageSize.
     */
    private final Integer maxPageSize;

    /**
     * Constructor for ListWorkersOptions.
     * @param workerStateSelector workerStateSelector.
     * @param channelId channelId.
     * @param queueId queueId.
     * @param hasCapacity hasCapacity.
     * @param maxPageSize maxPageSize.
     */
    public ListWorkersOptions(WorkerStateSelector workerStateSelector, String channelId, String queueId, boolean hasCapacity, Integer maxPageSize) {
        this.workerStateSelector = workerStateSelector;
        this.channelId = channelId;
        this.queueId = queueId;
        this.hasCapacity = hasCapacity;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Returns workerStateSelector.
     * @return workerStateSelector.
     */
    public WorkerStateSelector getWorkerStateSelector() {
        return this.workerStateSelector;
    }

    /**
     * Returns channelId.
     * @return channelId.
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Returns queueId.
     * @return queueId.
     */
    public String getQueueId() {
        return this.queueId;
    }

    /**
     * Returns hasCapacity.
     * @return hasCapacity.
     */
    public Boolean getHasCapacity() {
        return this.hasCapacity;
    }

    /**
     * Returns maxPageSize.
     * @return maxPageSize.
     */
    public Integer getMaxPageSize() {
        return maxPageSize;
    }
}
