// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.implementation.changefeed.implementation.FeedRangeGoneHandler.FeedRangeGoneHandler;
import reactor.core.publisher.Mono;

/**
 * READ DocDB partitions and create leases if they do not exist.
 */
public interface PartitionSynchronizer {
    /**
     * Creates missing leases.
     *
     * @return a deferred computation of this operation.
     */
    Mono<Void> createMissingLeases();

    /**
     * Get the feed range gone handler.
     *
     * @param lease the lease.
     * @return a {@link FeedRangeGoneHandler}.
     */
    Mono<FeedRangeGoneHandler> getFeedRangeGoneHandler(Lease lease);
}
