// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;


import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.FeedResponse;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverContext;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.PartitionCheckpointer;
import reactor.core.publisher.Mono;

/**
 * Implementation for ChangeFeedObserverContext.
 */
class ChangeFeedObserverContextImpl implements ChangeFeedObserverContext {
    private final PartitionCheckpointer checkpointer;
    private final String partitionKeyRangeId;
    private final FeedResponse<CosmosItemProperties> feedResponse;
    private String responseContinuation;

    public ChangeFeedObserverContextImpl(String leaseToken) {
        this.partitionKeyRangeId = leaseToken;
        this.checkpointer = null;
        this.feedResponse = null;
    }

    public ChangeFeedObserverContextImpl(String leaseToken, FeedResponse<CosmosItemProperties> feedResponse, PartitionCheckpointer checkpointer) {
        this.partitionKeyRangeId = leaseToken;
        this.feedResponse = feedResponse;
        this.checkpointer = checkpointer;
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
        this.responseContinuation = this.feedResponse.continuationToken();

        return this.checkpointer.checkpointPartition(this.responseContinuation);
    }

    /**
     * @return the id of the partition for the current event.
     */
    @Override
    public String getPartitionKeyRangeId() {
        return this.partitionKeyRangeId;
    }

    /**
     * @return the response from the underlying call.
     */
    @Override
    public FeedResponse<CosmosItemProperties> getFeedResponse() {
        return this.feedResponse;
    }

}
