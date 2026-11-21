// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;


import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.publisher.Mono;

/**
 * Implementation for ChangeFeedObserverContext.
 */
public class ChangeFeedObserverContextImpl<T> implements ChangeFeedObserverContext<T> {
    private final PartitionCheckpointer checkpointer;
    private final String leaseToken;
    private final FeedResponse<T> feedResponse;
    private final ChangeFeedState continuationState;
    private final boolean initialCheckpointRequired;


    public ChangeFeedObserverContextImpl(String leaseToken) {
        this.leaseToken = leaseToken;
        this.checkpointer = null;
        this.feedResponse = null;
        this.continuationState = null;
        this.initialCheckpointRequired = false;
    }

    public ChangeFeedObserverContextImpl(String leaseToken,
                                         FeedResponse<T> feedResponse,
                                         ChangeFeedState continuationState,
                                         PartitionCheckpointer checkpointer) {
        this(leaseToken, feedResponse, continuationState, checkpointer, false);
    }

    public ChangeFeedObserverContextImpl(String leaseToken,
                                        FeedResponse<T> feedResponse,
                                        ChangeFeedState continuationState,
                                        PartitionCheckpointer checkpointer,
                                        boolean initialCheckpointRequired) {
        this.leaseToken = leaseToken;
        this.feedResponse = feedResponse;
        this.checkpointer = checkpointer;
        this.continuationState = continuationState;
        this.initialCheckpointRequired = initialCheckpointRequired;
    }

    /**
     * Checkpoints progress of a stream. This method is valid only if manual checkpoint was configured.
     * <p>
     * Client may accept multiple change feed batches to process in parallel.
     *   Once first N document processing was finished the client can call checkpoint on the last completed batches in the row.
     *   In case of automatic checkpointing this is method throws.
     *
     * @return a deferred computation of this call.
     */
    @Override
    public Mono<Lease> checkpoint() {
        return this.checkpointer.checkpointPartition(this.continuationState);
    }

    @Override
    public boolean isInitialCheckpointRequired() {
        return this.initialCheckpointRequired;
    }

    @Override
    public Mono<Lease> checkpointOnShutdown() {
        return this.checkpointer.checkpointPartitionOnShutdown(this.continuationState);
    }

    /**
     * @return the id of the partition for the current event.
     */
    @Override
    public String getLeaseToken() {
        return this.leaseToken;
    }

    /**
     * @return the response from the underlying call.
     */
    @Override
    public FeedResponse<T> getFeedResponse() {
        return this.feedResponse;
    }
}
