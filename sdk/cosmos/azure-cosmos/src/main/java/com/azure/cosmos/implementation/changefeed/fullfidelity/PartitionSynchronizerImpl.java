// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseContainer;
import com.azure.cosmos.implementation.changefeed.LeaseManager;
import com.azure.cosmos.implementation.changefeed.PartitionSynchronizer;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
        return this.enumPartitionKeyRanges()
                   .collectList()
                   .flatMap(pkRangeList -> this.createLeases(pkRangeList).then())
                   .onErrorResume(throwable -> {
                       logger.error("Create lease failed", throwable);
                       return Mono.empty();
                   });
    }

    @Override
    public Flux<Lease> splitPartition(Lease lease) {
        if (lease == null) {
            throw new IllegalArgumentException("lease");
        }

        final String leaseToken = lease.getLeaseToken();
        final Range<String> epkRange = ((FeedRangeEpkImpl) lease.getFeedRange()).getRange();

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

        logger.info("Partition {} with feed range {} is gone due to split; will attempt to resume using continuation token {}.", leaseToken, epkRange, lastContinuationToken);

        // After a split, the children are either all or none available
        return this.enumPartitionKeyRanges()
                   .filter(pkRange -> {
                        if (epkRange.getMin().equals(pkRange.getMinInclusive()) || epkRange.getMax().equals(pkRange.getMaxExclusive())) {
                            //  This lease exists, no need to create one for this pkRange
                            return false;
                        }
                        return true;
                    })
                    .flatMap(pkRange -> {
                        FeedRangeEpkImpl feedRangeEpk = new FeedRangeEpkImpl(pkRange.toRange());
                        return leaseManager.createLeaseIfNotExist(feedRangeEpk, null);
                    }, this.degreeOfParallelism)
                    .map(newLease -> {
                        logger.info("Partition {} split into new partition and continuation token {}.", newLease.getLeaseToken(), lastContinuationToken);
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
                logger.warn("Exception occurred while reading partition key range feed", throwable);
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
                                   if (epkRange.getMin().equals(pkRange.getMinInclusive()) || epkRange.getMax().equals(pkRange.getMaxExclusive())) {
                                       //  This lease exists, no need to create one for this pkRange
                                       return true;
                                   }
                                   return false;
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
}
