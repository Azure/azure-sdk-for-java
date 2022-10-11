// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.v1.feedRangeGoneHandler;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FeedRangeGoneSplitHandler implements FeedRangeGoneHandler {
    private static final Logger logger = LoggerFactory.getLogger(FeedRangeGoneSplitHandler.class);
    private final Lease lease;
    private final List<PartitionKeyRange> overlappingRanges;
    private final LeaseManager leaseManager;
    private final boolean removeCurrentLease;

    public FeedRangeGoneSplitHandler(Lease lease, List<PartitionKeyRange> overlappingRanges, LeaseManager leaseManager) {
        checkNotNull(lease, "Argument 'lease' can not be null");
        checkNotNull(overlappingRanges, "Argument 'overlappingRanges' can not be null");
        checkNotNull(leaseManager, "Argument 'leaseManager' can not be null");

        this.lease = lease;
        this.overlappingRanges = overlappingRanges;
        this.leaseManager = leaseManager;

        // A flag to indicate to upstream whether the current lease which we get FeedRangeGoneException should be removed.
        // For split scenario, no matter whether it is partition based lease or epk based lease, it will be replaced by the child leases
        // so we need to remove the current lease in the end.
        this.removeCurrentLease = true;
    }

    @Override
    public Flux<Lease> handlePartitionGone() {
        //TODO: make sure we are putting feedRangeEpk in the lease token
        FeedRangeEpkImpl splitRange = (FeedRangeEpkImpl) lease.getFeedRange();
        AtomicReference<String> min = new AtomicReference<>(splitRange.getRange().getMin());
        AtomicReference<String> max = new AtomicReference<>(splitRange.getRange().getMax());

        List<String> leaseTokens = new ArrayList<>();

        return Flux.just(overlappingRanges)
                .flatMapIterable(pkRanges -> {

                    // Create new leases starting from the current min and ending in the current max and across the ordered list of partitions
                    // Example:
                    // Current lease epk range: AA-DD
                    // It split into 3 partition ranges: []-BB, BB-CC, CC-[]
                    // So we will create EPKRange lease for AA-BB, BB-CC, CC-DD

                    List<FeedRangeEpkImpl> epkRanges = new ArrayList<>();
                    for (int i = 0; i < pkRanges.size() - 1; i++) {
                        Range<String> range = pkRanges.get(i).toRange();
                        epkRanges.add(new FeedRangeEpkImpl(new Range<>(min.get(), range.getMax(), true, false)));
                        min.set(pkRanges.get(i).getMaxExclusive());
                    }

                    // add the last range with the original max and the last min
                    // TODO: make sure do not add range with min = max
                    epkRanges.add(new FeedRangeEpkImpl(new Range<>(min.get(), max.get(), true, false)));
                    return epkRanges;
                })
                .flatMap(newEpkRange -> {
                    // TODO: Annie: what if not all child leases are created
                    // Should we change to use batch transaction?
                    return this.leaseManager.createLeaseIfNotExist(newEpkRange, this.lease.getContinuationToken())
                            .doOnSuccess(newLease -> leaseTokens.add(newLease.getLeaseToken()));
                })
                .doOnComplete(() -> {
                    logger.info(
                            "Lease with token {} split into {}",
                            this.lease.getLeaseToken(),
                            StringUtils.join(leaseTokens, ","));
                });
    }

    @Override
    public boolean shouldDeleteCurrentLease() {
        return this.removeCurrentLease;
    }
}
