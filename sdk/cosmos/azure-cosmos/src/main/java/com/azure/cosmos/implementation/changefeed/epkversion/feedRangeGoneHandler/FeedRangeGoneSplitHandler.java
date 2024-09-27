// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
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
    private final boolean shouldSkipDirectLeaseAssignment;

    public FeedRangeGoneSplitHandler(
        Lease lease,
        List<PartitionKeyRange> overlappingRanges,
        LeaseManager leaseManager,
        int maxScaleCount) {
        checkNotNull(lease, "Argument 'lease' can not be null");
        checkNotNull(overlappingRanges, "Argument 'overlappingRanges' can not be null");
        checkNotNull(leaseManager, "Argument 'leaseManager' can not be null");

        this.lease = lease;
        this.overlappingRanges = overlappingRanges;
        this.leaseManager = leaseManager;

        // A flag to indicate to upstream whether the current lease which we get FeedRangeGoneException should be removed.
        // For split, the parent lease will be replaced by the child leases. so we need to remove the current lease in the end.
        this.removeCurrentLease = true;

        // If maxScaleCount is configured, then all lease assignments should go through load balancer
        // It will make sure the change feed processor instance always honor the maxScaleCount config
        this.shouldSkipDirectLeaseAssignment = maxScaleCount > 0;
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
                    epkRanges.add(new FeedRangeEpkImpl(new Range<>(min.get(), max.get(), true, false)));
                    return epkRanges;
                })
                .flatMap(newEpkRange -> {
                    // TODO: Annie: what if not all child leases are created
                    // Should we change to use batch transaction?
                    String effectiveChildLeaseContinuationToken = this.getEffectiveChildLeaseContinuationToken(
                            newEpkRange,
                            this.lease.getContinuationToken());

                    return this.leaseManager
                            .createLeaseIfNotExist(newEpkRange, effectiveChildLeaseContinuationToken)
                            .map(newLease -> {
                                leaseTokens.add(newLease.getLeaseToken());
                                return newLease;
                            });
                })
                .doOnComplete(() -> {
                    logger.info(
                            "Lease with token {} split into {}",
                            this.lease.getLeaseToken(),
                            StringUtils.join(leaseTokens, ","));
                });
    }


    private String getEffectiveChildLeaseContinuationToken(FeedRangeEpkImpl childLeaseFeedRange, String parentLeaseCT) {
        String childLeaseCT = parentLeaseCT;
        if (StringUtils.isNotEmpty(parentLeaseCT)) {
            ChangeFeedState changeFeedState = ChangeFeedStateV1.fromString(parentLeaseCT);

            FeedRangeContinuation effectiveFeedRangeContinuation = FeedRangeContinuation.create(
                    changeFeedState.getContainerRid(),
                    childLeaseFeedRange,
                    childLeaseFeedRange.getRange());
            effectiveFeedRangeContinuation.replaceContinuation(
                    changeFeedState.getContinuation().getCurrentContinuationToken().getToken(), true);

            changeFeedState.setContinuation(effectiveFeedRangeContinuation);
            childLeaseCT = changeFeedState.toString();
        }

        return childLeaseCT;
    }

    @Override
    public boolean shouldDeleteCurrentLease() {
        return this.removeCurrentLease;
    }

    @Override
    public boolean shouldSkipDirectLeaseAssignment() {
        return this.shouldSkipDirectLeaseAssignment;
    }
}
