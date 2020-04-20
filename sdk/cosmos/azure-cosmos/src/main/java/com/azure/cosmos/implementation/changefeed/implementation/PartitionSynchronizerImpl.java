// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.PartitionSynchronizer;
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

    public PartitionSynchronizerImpl(
            ChangeFeedContextClient documentClient,
            CosmosAsyncContainer collectionSelfLink,
            LeaseContainer leaseContainer,
            LeaseManager leaseManager,
            int degreeOfParallelism,
            int maxBatchSize) {

        this.documentClient = documentClient;
        this.collectionSelfLink = collectionSelfLink;
        this.leaseContainer = leaseContainer;
        this.leaseManager = leaseManager;
        this.degreeOfParallelism = degreeOfParallelism;
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    public Mono<Void> createMissingLeases() {
        return this.enumPartitionKeyRanges()
            .map(partitionKeyRange -> {
                // TODO: log the partition getKey ID found.
                return partitionKeyRange.getId();
            })
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

        String leaseToken = lease.getLeaseToken();
        String lastContinuationToken = lease.getContinuationToken();

        logger.info("Partition {} is gone due to split.", leaseToken);

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
                logger.info("Partition {} split into new partition with lease token {}.", leaseToken, newLease.getLeaseToken());
                return newLease;
            });
    }

    private Flux<PartitionKeyRange> enumPartitionKeyRanges() {
        String partitionKeyRangesPath = extractContainerSelfLink(this.collectionSelfLink);
        FeedOptions feedOptions = new FeedOptions();
        ModelBridgeInternal.setFeedOptionsContinuationTokenAndMaxItemCount(feedOptions, null, this.maxBatchSize);

        return this.documentClient.readPartitionKeyRangeFeed(partitionKeyRangesPath, feedOptions)
            .map(partitionKeyRangeFeedResponse -> partitionKeyRangeFeedResponse.getResults())
            .flatMap(partitionKeyRangeList -> Flux.fromIterable(partitionKeyRangeList))
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
