// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;


import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

/**
 * Implementation for ChangeFeedObserverContext.
 */
class ChangeFeedObserverContextImpl implements ChangeFeedObserverContext {
    private final PartitionCheckpointer checkpointer;
    private final String partitionKeyRangeId;
    private final FeedResponse<ChangeFeedProcessorItem> feedResponse;
    private final ChangeFeedState continuationState;


    public ChangeFeedObserverContextImpl(String leaseToken) {
        this.partitionKeyRangeId = leaseToken;
        this.checkpointer = null;
        this.feedResponse = null;
        this.continuationState = null;
    }

    public ChangeFeedObserverContextImpl(String leaseToken,
                                         FeedResponse<ChangeFeedProcessorItem> feedResponse,
                                         ChangeFeedState continuationState,
                                         PartitionCheckpointer checkpointer) {
        this.partitionKeyRangeId = leaseToken;
        this.feedResponse = feedResponse;
        this.checkpointer = checkpointer;
        this.continuationState = continuationState;
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
    public FeedResponse<JsonNode> getFeedResponse() {
        throw new UnsupportedOperationException("getFeedResponseV1() should be called instead for Full Fidelity");
    }

    @Override
    public FeedResponse<ChangeFeedProcessorItem> getFeedResponseV1() {
        return this.feedResponse;
    }
}
