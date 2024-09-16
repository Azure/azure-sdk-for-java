// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler.FeedRangeGoneHandler;
import com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler.FeedRangeGoneMergeHandler;
import com.azure.cosmos.implementation.changefeed.epkversion.feedRangeGoneHandler.FeedRangeGoneSplitHandler;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Implementation for the partition synchronizer.
 */
class PartitionSynchronizerImpl implements PartitionSynchronizer {
    private static final Comparator<Range<String>> MIN_RANGE_COMPARATOR = new Range.MinComparator<>();
    private static final Comparator<Range<String>> MAX_RANGE_COMPARATOR = new Range.MaxComparator<>();

    private final Logger logger = LoggerFactory.getLogger(PartitionSynchronizerImpl.class);
    private final ChangeFeedContextClient documentClient;
    private final String collectionSelfLink;
    private final LeaseContainer leaseContainer;
    private final LeaseManager leaseManager;
    private final int degreeOfParallelism;
    private final int maxBatchSize;

    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final ChangeFeedMode changeFeedMode;

    public PartitionSynchronizerImpl(
        ChangeFeedContextClient documentClient,
        String collectionSelfLink,
        LeaseContainer leaseContainer,
        LeaseManager leaseManager,
        int degreeOfParallelism,
        int maxBatchSize,
        ChangeFeedProcessorOptions changeFeedProcessorOptions,
        ChangeFeedMode changeFeedMode) {

        this.documentClient = documentClient;
        this.collectionSelfLink = collectionSelfLink;
        this.leaseContainer = leaseContainer;
        this.leaseManager = leaseManager;
        this.degreeOfParallelism = degreeOfParallelism;
        this.maxBatchSize = maxBatchSize;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.changeFeedMode = changeFeedMode;
    }

    @Override
    public Mono<Void> createMissingLeases() {
        return this.documentClient.getOverlappingRanges(PartitionKeyInternalHelper.FullRange, true)
                .flatMap(pkRangeList -> this.createLeases(pkRangeList).then())
                .onErrorResume(throwable -> {
                    logger.error("Create lease failed", throwable);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> createMissingLeases(List<Lease> pkRangeIdVersionLeases) {
        return this.documentClient.getOverlappingRanges(PartitionKeyInternalHelper.FullRange, true)
            .flatMap(pkRangeList -> this.createLeases(pkRangeList, pkRangeIdVersionLeases).then())
            .doOnError(throwable -> logger.error("Create missing leases from pkRangeIdVersion leases failed", throwable));
    }

    @Override
    public Mono<FeedRangeGoneHandler> getFeedRangeGoneHandler(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");
        final String leaseToken = lease.getLeaseToken();
        final String lastContinuationToken = lease.getContinuationToken();

        logger.info(
                "Lease with token {} is gone due to split or merge; will attempt to resume using continuation token {}.",
                leaseToken,
                lastContinuationToken);

        return this.documentClient.getOverlappingRanges(((FeedRangeEpkImpl)lease.getFeedRange()).getRange(), true)
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
                        return Mono.just(new FeedRangeGoneSplitHandler(lease, pkRangeList, this.leaseManager, this.changeFeedProcessorOptions.getMaxScaleCount()));
                    }

                    // Merge
                    return Mono.just(new FeedRangeGoneMergeHandler(lease, pkRangeList.get(0)));
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
    private Flux<Lease> createLeases(List<PartitionKeyRange> partitionKeyRanges) {
        return this.leaseContainer
            .getAllLeases()
            //  collecting this as a list is important because it will still call flatMapMany even if the list is empty.
            //  when initializing, all leases will return empty list.
            .collectList()
            .flatMapMany(leaseList -> {
                return Flux.fromIterable(partitionKeyRanges)
                           .flatMap(pkRange -> {
                               // check if there are epk based leases for the partitionKeyRange
                               // If there is at least one, then we assume there are others
                               // that cover the rest of the full partition range
                               // based on the fact that the lease store was always
                               // initialized for the full collection
                               // TODO:(kuthapar) what if some epkRange did not create successfully?
                               boolean anyMatch = leaseList.stream().anyMatch(lease -> {
                                   Range<String> epkRange = ((FeedRangeEpkImpl) lease.getFeedRange()).getRange();
                                   //  We are creating the lease for the whole pkRange, so even if we find at least one, we should be good.
                                   //  This lease exists, no need to create one for this pkRange
                                   return epkRange.getMin().equals(pkRange.getMinInclusive()) || epkRange.getMax().equals(pkRange.getMaxExclusive());
                               });
                               //   If there is no match, it means leases don't exist for these pkranges.
                               if (!anyMatch) {
                                   return Mono.just(pkRange);
                               }
                               return Mono.empty();
                           }).flatMap(pkRange -> {
                                FeedRangeEpkImpl feedRangeEpk = new FeedRangeEpkImpl(pkRange.toRange());
                                //  We are creating the lease for the whole pkRange.
                                return leaseManager.createLeaseIfNotExist(feedRangeEpk, null);
                                }, this.degreeOfParallelism);
            });
    }

    /***
     * Create the missing epk version leases based on the pkRangeId version leases.
     *
     * NOTE: This method should only be used during bootstrap stage. It is split safe, but not merge safe.
     *
     * @param partitionKeyRanges a list of all partition key ranges.
     * @param pkRangeIdVersionLeases a list of pkRangeId version leases
     *
     * @return a deferred computation of this call.
     */
    private Flux<Lease> createLeases(List<PartitionKeyRange> partitionKeyRanges, List<Lease> pkRangeIdVersionLeases) {
        return this.leaseContainer.getAllLeases()
            .collectList()
            .flatMapMany(existingEpkVersionLeases -> {
                Map<String, Lease> pkRangeIdVersionLeaseMap =
                    pkRangeIdVersionLeases.stream().collect(Collectors.toMap(lease -> lease.getLeaseToken(), lease -> lease));

                return Flux.fromIterable(partitionKeyRanges)
                    .flatMap(partitionKeyRange -> {
                        boolean anyEpkVersionLeaseMatch = existingEpkVersionLeases.stream().anyMatch(lease -> {
                            Range<String> epkRange = ((FeedRangeEpkImpl) lease.getFeedRange()).getRange();
                            return MIN_RANGE_COMPARATOR.compare(epkRange, partitionKeyRange.toRange()) <= 0
                                && MAX_RANGE_COMPARATOR.compare(epkRange, partitionKeyRange.toRange()) >= 0;
                        });

                        if (anyEpkVersionLeaseMatch) {
                            // epk version lease has already been created for the pkRange, skip
                            return Mono.empty();
                        } else {
                            // The epk version lease does not exist, create one from pkRangeId version lease
                            List<String> possibleMatchingPkRangeId = new ArrayList<>();
                            possibleMatchingPkRangeId.add(partitionKeyRange.getId());
                            if (partitionKeyRange.getParents() != null) {
                                possibleMatchingPkRangeId.addAll(partitionKeyRange.getParents());
                            }

                            List<Lease> matchedPkRangeIdVersionLeases = new ArrayList<>();
                            for (String pkRangeId : possibleMatchingPkRangeId) {
                                if (pkRangeIdVersionLeaseMap.containsKey(pkRangeId)) {
                                    matchedPkRangeIdVersionLeases.add(pkRangeIdVersionLeaseMap.get(pkRangeId));
                                }
                            }

                            if (matchedPkRangeIdVersionLeases.size() == 0) {
                                return Mono.error(new IllegalStateException("Can not find pkRangeId version lease"));
                            } else if (matchedPkRangeIdVersionLeases.size() == 1) {
                                return Mono.just(Pair.of(partitionKeyRange, matchedPkRangeIdVersionLeases.get(0)));
                            } else {
                                return Mono.error(
                                    new IllegalStateException(
                                        "A merge has happened, creating epk version lease from pkRangeId version lease is not supported"));
                            }
                        }
                    })
                    .flatMap(pair -> {
                        FeedRangeEpkImpl feedRangeEpk = new FeedRangeEpkImpl(pair.getLeft().toRange());
                        String leaseContinuationToken = this.getLeaseContinuationToken(
                            feedRangeEpk,
                            pair.getRight().getContinuationToken());
                        return leaseManager.createLeaseIfNotExist(feedRangeEpk, leaseContinuationToken);
                    }, this.degreeOfParallelism);
            });
    }

    private String getLeaseContinuationToken(
        FeedRangeEpkImpl feedRangeEpk,
        String etag) {
        FeedRangeContinuation feedRangeContinuation = FeedRangeContinuation.create(
            this.collectionSelfLink,
            feedRangeEpk,
            feedRangeEpk.getRange());
        feedRangeContinuation.replaceContinuation(etag, true);

        ChangeFeedState changeFeedState =  new ChangeFeedStateV1(
            this.collectionSelfLink,
            feedRangeEpk,
            this.changeFeedMode,
            PartitionProcessorHelper.getStartFromSettings(
                feedRangeEpk,
                this.changeFeedProcessorOptions,
                this.changeFeedMode),
            feedRangeContinuation);

        return changeFeedState.toString();
    }
}
