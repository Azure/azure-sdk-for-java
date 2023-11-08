// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler.FeedRangeGoneHandler;
import reactor.core.publisher.Mono;

import java.util.List;

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

    /***
     * Create epk version leases based on pkRangeId version leases.
     *
     * @param pkRangeIdVersionLeases partitionKeyRangeId based leases.
     * @return a deferred computation of this operation.
     */
    Mono<Void> createMissingLeases(List<Lease> pkRangeIdVersionLeases);

    /***
     * Get the feedRangeGone handler based on whether it is merge or split.
     *
     * @param lease then lease of where {@link FeedRangeGoneException} exception happened.
     * @return the {@link FeedRangeGoneHandler}.
     */
    Mono<FeedRangeGoneHandler> getFeedRangeGoneHandler(Lease lease);
}

