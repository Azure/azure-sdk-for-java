// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;


import com.azure.cosmos.implementation.CosmosItemProperties;
import com.azure.cosmos.FeedResponse;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

/**
 * Implementation for ChangeFeedObserverContext.
 */
class ChangeFeedObserverContextImpl implements ChangeFeedObserverContext {
    private final PartitionCheckpointer checkpointer;
    private final String partitionKeyRangeId;
    private final FeedResponse<JsonNode> feedResponse;
    private String responseContinuation;

    public ChangeFeedObserverContextImpl(String leaseToken) {
        this.partitionKeyRangeId = leaseToken;
        this.checkpointer = null;
        this.feedResponse = null;
    }

    public ChangeFeedObserverContextImpl(String leaseToken, FeedResponse<JsonNode> feedResponse,
                                         PartitionCheckpointer checkpointer) {
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
        this.responseContinuation = this.feedResponse.getContinuationToken();

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
    public FeedResponse<JsonNode> getFeedResponse() {
        return this.feedResponse;
    }

}
