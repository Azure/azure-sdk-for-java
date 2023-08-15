// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.models.CosmosBulkOperationResponse;
import com.azure.cosmos.models.CosmosItemOperation;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Queue that keeps track of the status for retries and all the failed items for a specific partition key and id combination
 */
class StatusQueue<TContext> {

    enum Status {
        /** All the items in the queue should be retried after failure */
        Retry,
        /** Indicates a partition based gone happened so items in the queue should be added to the main sink */
        PartitionBasedGone,
        /** Indicates item will not be retried and items in the queue will be failed fast */
        NoRetry,
    }
    private final Queue<CosmosItemOperation> queue;
    private Status status;

    /** Used when the id and partition key combination should not be retried
     * and want to fail fast the rest of the duplicate items */
    private CosmosBulkOperationResponse<TContext> response;

    public StatusQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Wraps the add method of the concurrent linked queue to make sure no duplicates are added.
     * @param cosmosItemOperation the itemOperation to be added
     */
    public void add(CosmosItemOperation cosmosItemOperation) {
        if (!queue.contains(cosmosItemOperation)) {
            queue.add(cosmosItemOperation);
        }
    }

    public boolean isInitialFailedItem(CosmosItemOperation operation) {
        return operation.equals(queue.peek());
    }

    public CosmosItemOperation poll() {
        return queue.poll();
    }
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public CosmosBulkOperationResponse<TContext> getResponse() {
        return response;
    }

    public void setResponse(CosmosBulkOperationResponse<TContext> response) {
        this.response = response;
    }

    public Queue<CosmosItemOperation> getQueue() {
        return queue;
    }
}
