package com.azure.cosmos.implementation.changefeed.v1;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.changefeed.v1.feedRangeGoneHandler.FeedRangeGoneHandler;
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

    /***
     * Get the feedRangeGone handler based on whether it is merge or split.
     *
     * @param lease then lease of where {@link FeedRangeGoneException} exception happened.
     * @return the {@link FeedRangeGoneHandler}.
     */
    Mono<FeedRangeGoneHandler> getFeedRangeGoneHandler(Lease lease);
}

