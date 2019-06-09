/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.changefeed.internal;


import com.azure.data.cosmos.ChangeFeedObserverContext;
import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.changefeed.PartitionCheckpointer;
import com.azure.data.cosmos.FeedResponse;
import reactor.core.publisher.Mono;

/**
 * Implementation for ChangeFeedObserverContext.
 */
public class ChangeFeedObserverContextImpl implements ChangeFeedObserverContext {
    private final PartitionCheckpointer checkpointer;
    private final String partitionKeyRangeId;
    private final FeedResponse<CosmosItemProperties> feedResponse;
    private String responseContinuation;

    public ChangeFeedObserverContextImpl(String leaseToken) {
        this.partitionKeyRangeId = leaseToken;
        this.checkpointer = null;
        this.feedResponse = null;
    }

    public ChangeFeedObserverContextImpl(String leaseToken, FeedResponse<CosmosItemProperties> feedResponse, PartitionCheckpointer checkpointer)
    {
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
    public Mono<Void> checkpoint() {
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
