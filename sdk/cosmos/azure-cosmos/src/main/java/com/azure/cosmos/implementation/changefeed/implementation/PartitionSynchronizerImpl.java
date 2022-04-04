// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.PartitionSynchronizer;
import com.azure.cosmos.implementation.changefeed.implementation.feedRangeGoneHandler.FeedRangeGoneHandler;
import com.azure.cosmos.implementation.changefeed.implementation.feedRangeGoneHandler.FeedRangeGoneMergeHandler;
import com.azure.cosmos.implementation.changefeed.implementation.feedRangeGoneHandler.FeedRangeGoneSplitHandler;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseCore;
import com.azure.cosmos.implementation.changefeed.implementation.leaseManagement.ServiceItemLeaseEpk;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for the partition synchronizer.
 */
public class PartitionSynchronizerImpl implements PartitionSynchronizer {
    private final Logger logger = LoggerFactory.getLogger(PartitionSynchronizerImpl.class);
    private final ChangeFeedContextClient documentClient;
    private final LeaseContainer leaseContainer;
    private final LeaseManager leaseManager;
    private final int degreeOfParallelism;

    public PartitionSynchronizerImpl(
            ChangeFeedContextClient documentClient,
            LeaseContainer leaseContainer,
            LeaseManager leaseManager,
            int degreeOfParallelism) {

        this.documentClient = documentClient;
        this.leaseContainer = leaseContainer;
        this.leaseManager = leaseManager;
        this.degreeOfParallelism = degreeOfParallelism;
    }

    @Override
    public Mono<Void> createMissingLeases() {
        return this.documentClient.getOverlappingRanges(PartitionKeyInternalHelper.FullRange)
                .flatMap(pkRangeList -> this.createLeases(pkRangeList).then())
                .onErrorResume(throwable -> {
                    logger.error("Create lease failed", throwable);
                    return Mono.empty();
                });
    }

    /***
     * Handle a Partition Gone response and decide what to do based on the type of lease.
     *
     * @param lease the lease.
     *
     * @return Returns a flux of leases to create.
     */
    @Override
    public Mono<FeedRangeGoneHandler> getFeedRangeGoneHandler(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");

        final String leaseToken = lease.getLeaseToken();
        final String lastContinuationToken = lease.getContinuationToken();

        logger.info(
                "Lease with token {} is gone due to split or merge; will attempt to resume using continuation token {}.",
                leaseToken,
                lastContinuationToken);

        return this.documentClient.getOverlappingRanges(((FeedRangeEpkImpl)lease.getFeedRange()).getRange())
                .flatMap(pkRangeList -> {
                    if (pkRangeList.size() == 0) {
                        logger.error("Lease with token {} is gone but we failed to find at least one child range", leaseToken);
                        return Mono.error(
                                new RuntimeException(
                                        String.format(
                                                "Lease %s is gone but we failed to find at least one child partition",
                                                leaseToken)));
                    }

                    if (pkRangeList.size() > 1) {
                        // Split: More than two children spanning the pkRange
                        return Mono.just(new FeedRangeGoneSplitHandler(lease, pkRangeList, this.leaseManager));
                    }

                    // Merge
                    return Mono.just(new FeedRangeGoneMergeHandler(lease, pkRangeList.get(0), this.leaseManager));
                });
    }

    /**
     * Creates leases if they do not exist. This might happen on initial start or if some leases was unexpectedly lost.
     * <p>
     * Leases are created without the continuation token. It means partitions will be read according to
     *   'From Beginning' or 'From current time'.
     * Same applies also to split partitions. We do not search for parent lease and take continuation token since this
     *   might end up of reprocessing all the events since the split.
     *
     * @param partitionKeyRanges a list of all partition key ranges.
     * @return a deferred computation of this call.
     */
    private Flux<Lease> createLeases(List<PartitionKeyRange> partitionKeyRanges)
    {
        return this.leaseContainer.getAllLeases()
                .collectList()
                .flatMapMany(leaseList -> {
                    // There will be two kinds of lease
                    // One is pkRange based -> ServiceItemLeaseCore
                    // One is epk based -> ServiceItemLeaseEpk
                    // If for a certain partitionKeyRange, either above types lease exists, then there is no need to create a new lease file.
                    Set<String> pkRangeBasedLeases = leaseList
                            .stream().filter(lease -> lease instanceof ServiceItemLeaseCore).map(Lease::getLeaseToken).collect(Collectors.toSet());
                    return Flux.fromIterable(partitionKeyRanges)
                            .flatMap(pkRange -> {
                                // check whether there is pkRange based lease exists
                                if (pkRangeBasedLeases.contains(pkRange.getId())) {
                                    return Mono.empty();
                                }

                                // check if there are epk based leases for the partitionKeyRange
                                // If there is at least one, then we assume there are others that cover the rest the full partition range
                                // based on the fact that the lease store was always initialized for the full collection
                                // TODO: Annie: what if some epkRange did not create successfully?
                                if (leaseList.stream().anyMatch(lease -> {
                                    if (lease instanceof ServiceItemLeaseEpk && lease.getFeedRange() instanceof FeedRangeEpkImpl) {
                                        Range<String> epkRange = ((FeedRangeEpkImpl) lease.getFeedRange()).getRange();
                                        return epkRange.getMin().equals(pkRange.getMinInclusive()) || epkRange.getMax().equals(pkRange.getMaxExclusive());
                                    }
                                    return false;
                                })) {
                                    return Mono.empty();
                                }

                                return Mono.just(pkRange);
                            });
                })
                .flatMap(
                        pkRangeNeedToAddLease ->
                                this.leaseManager.createLeaseIfNotExist(pkRangeNeedToAddLease, null), this.degreeOfParallelism);
    }
}
