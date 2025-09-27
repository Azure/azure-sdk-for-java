// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private final ChangeFeedProcessorOptions changeFeedProcessorOptions;
    private final String hostName;

    public PartitionSynchronizerImpl(
            ChangeFeedContextClient documentClient,
            CosmosAsyncContainer collectionSelfLink,
            LeaseContainer leaseContainer,
            LeaseManager leaseManager,
            int degreeOfParallelism,
            int maxBatchSize,
            String collectionResourceId,
            ChangeFeedProcessorOptions changeFeedProcessorOptions,
            String hostName) {

        this.documentClient = documentClient;
        this.collectionSelfLink = collectionSelfLink;
        this.leaseContainer = leaseContainer;
        this.leaseManager = leaseManager;
        this.degreeOfParallelism = degreeOfParallelism;
        this.maxBatchSize = maxBatchSize;
        this.collectionResourceId = collectionResourceId;
        this.changeFeedProcessorOptions = changeFeedProcessorOptions;
        this.hostName = hostName;
    }

    @Override
    public Mono<Void> createMissingLeases() {
        Map<String, List<String>> leaseTokenMap = new ConcurrentHashMap<>();

        String createMissingLeasesFlow = "createMissingLeases";

        return this.enumPartitionKeyRanges(createMissingLeasesFlow)
            .map(partitionKeyRange -> {
                leaseTokenMap.put(
                    partitionKeyRange.getId(),
                    partitionKeyRange.getParents() == null ? Collections.emptyList() : partitionKeyRange.getParents());
                return partitionKeyRange.getId();
            })
            .collectList()
            .flatMap( partitionKeyRangeIds -> {
                logger.info(
                    "Checking whether leases for any partition is missing - partitions - {}",
                    String.join(", ", partitionKeyRangeIds));
                return this.createLeases(leaseTokenMap).then();
            })
            .onErrorResume( throwable -> {
                logger.error("Failed to create missing leases.", throwable);
                return Mono.error(throwable);
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

        String splitFlow = "SplitFlow";

        // After a split, the children are either all or none available
        return this.enumPartitionKeyRanges(splitFlow)
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
                return this.leaseManager.createLeaseIfNotExist(addedRangeId, lastContinuationToken, lease.getProperties());
            }, this.degreeOfParallelism)
            .map(newLease -> {
                logger.info("Partition {} split into new partition {} and continuation token {}.", leaseToken, newLease.getLeaseToken(), lastContinuationToken);
                return newLease;
            });
    }

    private Flux<PartitionKeyRange> enumPartitionKeyRanges(String flowId) {
        logger.warn("Performing a ReadFeed of PartitionKeyRange initiated by [{}] : for CollectionLink : [{}] by Host : [{}] targeting LeasePrefix : [{}]",
            flowId, this.collectionSelfLink, this.hostName, this.changeFeedProcessorOptions.getLeasePrefix());

        return this.documentClient.getOverlappingRanges(PartitionKeyInternalHelper.FullRange, true)
            .doOnNext(responses -> {
                logger.warn("Obtained feed response with {} partition key ranges and flowId : {}",
                    responses.size(),
                    flowId);
            })
            .flatMapMany(Flux::fromIterable)
            .onErrorResume(throwable -> {
                logger.error("Failed to retrieve physical partition information.", throwable);
                return Flux.empty();
            });
    }

    /**
     * Creates leases if they do not exist for the partition or partition's parent partitions.
     * This might happen on initial start or if some lease was unexpectedly lost.
     * <p>
     * Leases are created without the continuation token. It means partitions will be read according to
     *   'From Beginning' or 'From current time'.
     * Same applies also to split partitions. We do not search for parent lease and take continuation token since this
     *   might end up of reprocessing all the events since the split.
     *
     * @param leaseTokenMap a map of all the lease tokens and their mapping parent lease tokens.
     * @return a deferred computation of this call.
     */
    private Flux<Lease> createLeases(Map<String, List<String>> leaseTokenMap)
    {
        List<String> leaseTokensToBeAdded = new ArrayList<>();
        return this.leaseContainer.getAllLeases()
            .map(lease -> lease.getLeaseToken())
            .collectList()
            .flatMapMany(existingLeaseTokens -> {
                // only create lease documents if there is no existing lease document matching the partition or its parent partitions
                leaseTokensToBeAdded.addAll(
                    leaseTokenMap.entrySet().stream()
                        .filter(entry -> !existingLeaseTokens.contains(entry.getKey()))
                        .filter(entry -> entry.getValue() == null ||
                            entry.getValue().isEmpty() ||
                            entry.getValue().stream().noneMatch(existingLeaseTokens::contains))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList())
                );

                logger.info("Missing lease documents for partitions: [{}]", String.join(", ", leaseTokensToBeAdded));
                return Flux.fromIterable(leaseTokensToBeAdded);
            })
            .flatMap(leaseTokenToBeAdded -> {
                logger.debug("Adding a new lease document for partition {}", leaseTokenToBeAdded);
                return this.leaseManager.createLeaseIfNotExist(leaseTokenToBeAdded, null);
            }, this.degreeOfParallelism)
            .map(lease -> {
                logger.info("Added new lease document for partition {}", lease.getLeaseToken());
                return lease;
            });
    }
}
