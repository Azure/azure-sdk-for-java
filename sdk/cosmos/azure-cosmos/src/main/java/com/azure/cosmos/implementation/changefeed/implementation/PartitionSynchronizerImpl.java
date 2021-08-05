// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.PartitionSynchronizer;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

import static com.azure.cosmos.BridgeInternal.extractContainerSelfLink;

/**
 * Implementation for the partition synchronizer.
 */
class PartitionSynchronizerImpl implements PartitionSynchronizer {
    private final Logger logger = LoggerFactory.getLogger(PartitionSynchronizerImpl.class);
    private final ChangeFeedContextClient documentClient;
    private final CosmosAsyncContainer collectionSelfLink;
    private final LeaseContainer leaseContainer;
    private final LeaseManager leaseManager;
    private final int degreeOfParallelism;
    private final int maxBatchSize;
    private final String collectionResourceId;

    public PartitionSynchronizerImpl(
            ChangeFeedContextClient documentClient,
            CosmosAsyncContainer collectionSelfLink,
            LeaseContainer leaseContainer,
            LeaseManager leaseManager,
            int degreeOfParallelism,
            int maxBatchSize,
            String collectionResourceId) {

        this.documentClient = documentClient;
        this.collectionSelfLink = collectionSelfLink;
        this.leaseContainer = leaseContainer;
        this.leaseManager = leaseManager;
        this.degreeOfParallelism = degreeOfParallelism;
        this.maxBatchSize = maxBatchSize;
        this.collectionResourceId = collectionResourceId;
    }

    @Override
    public Mono<Void> createMissingLeases() {
        // TODO: log the partition getKey ID found.
        return this.enumPartitionKeyRanges()
            .map(Resource::getId)
            .collectList()
            .flatMap( partitionKeyRangeIds -> {
                Set<String> leaseTokens = new HashSet<>(partitionKeyRangeIds);
                return this.createLeases(leaseTokens).then();
            })
            .onErrorResume( throwable -> {
                // TODO: log the exception.
                return Mono.empty();
            });
    }

    @Override
    public Flux<Lease> splitPartition(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        final String leaseToken = lease.getLeaseToken();

        // TODO fabianm - this needs more elaborate processing in case the initial
        // FeedRangeContinuation has continuation state for multiple feed Ranges
        // and with merge multiple CompositeContinuationItems
        // Means Split/Merge needs to be pushed into the FeedRangeContinuation
        // Will be necessary for merge anyway
        // but efficient testing only works if at least EPK filtering is available in Emulator
        // or at least Service - this will be part of the next set of changes
        // For now - no merge just simple V0 of lease contract
        // this simplification will work
        //
        //ChangeFeedState lastContinuationState = lease.getContinuationState(
        //    this.collectionResourceId,
        //    new FeedRangePartitionKeyRangeImpl(leaseToken)
        //);
        //
        //final String lastContinuationToken = lastContinuationState.getContinuation() != null ?
        //    lastContinuationState.getContinuation().getCurrentContinuationToken().getToken() :
        //    null;

        // "Push" ChangeFeedProcessor is not merge-proof currently. For such cases we need a specific handler that can
        // take multiple leases and "converge" them in a thread safe manner while also merging the various continuation
        // tokens for each merged lease.
        // We will directly reuse the original/parent continuation token as the seed for the new leases until then.
        final String lastContinuationToken = lease.getContinuationToken();

        logger.info("Partition {} is gone due to split; will attempt to resume using continuation token {}.", leaseToken, lastContinuationToken);

        // After a split, the children are either all or none available
        return this.enumPartitionKeyRanges()
            .filter(range -> range != null && range.getParents() != null && range.getParents().contains(leaseToken))
            .map(PartitionKeyRange::getId)
            .collectList()
            .flatMapMany(addedLeaseTokens -> {
                if (addedLeaseTokens.size() == 0) {
                    logger.error("Partition {} had split but we failed to find at least one child partition", leaseToken);
                    throw new RuntimeException(String.format("Partition %s had split but we failed to find at least one child partition", leaseToken));
                }
                return Flux.fromIterable(addedLeaseTokens);
            })
            .flatMap(addedRangeId -> {
                // Creating new lease.
                return this.leaseManager.createLeaseIfNotExist(addedRangeId, lastContinuationToken);
            }, this.degreeOfParallelism)
            .map(newLease -> {
                logger.info("Partition {} split into new partition with lease token {} and continuation token {}.", leaseToken, newLease.getLeaseToken(), lastContinuationToken);
                return newLease;
            });
    }

    private Flux<PartitionKeyRange> enumPartitionKeyRanges() {
        String partitionKeyRangesPath = extractContainerSelfLink(this.collectionSelfLink);
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();
        ModelBridgeInternal.setQueryRequestOptionsContinuationTokenAndMaxItemCount(cosmosQueryRequestOptions, null, this.maxBatchSize);

        return this.documentClient.readPartitionKeyRangeFeed(partitionKeyRangesPath, cosmosQueryRequestOptions)
            .map(FeedResponse::getResults)
            .flatMap(Flux::fromIterable)
            .onErrorResume(throwable -> {
                // TODO: Log the exception.
                return Flux.empty();
            });
    }

    /**
     * Creates leases if they do not exist. This might happen on initial start or if some lease was unexpectedly lost.
     * <p>
     * Leases are created without the continuation token. It means partitions will be read according to
     *   'From Beginning' or 'From current time'.
     * Same applies also to split partitions. We do not search for parent lease and take continuation token since this
     *   might end up of reprocessing all the events since the split.
     *
     * @param leaseTokens a hash set of all the lease tokens.
     * @return a deferred computation of this call.
     */
    private Flux<Lease> createLeases(Set<String> leaseTokens)
    {
        Set<String> addedLeaseTokens = new HashSet<>(leaseTokens);

        return this.leaseContainer.getAllLeases()
            .map(lease -> {
                if (lease != null) {
                    // Get leases after getting ranges, to make sure that no other hosts checked in continuation for
                    //   split partition after we got leases.
                    addedLeaseTokens.remove(lease.getLeaseToken());
                }

                return lease;
            })
            .thenMany(Flux.fromIterable(addedLeaseTokens)
                .flatMap( addedRangeId ->
                    this.leaseManager.createLeaseIfNotExist(addedRangeId, null), this.degreeOfParallelism)
                .map( lease -> {
                    // TODO: log the lease info that was added.
                    return lease;
                })
            );
    }
}
