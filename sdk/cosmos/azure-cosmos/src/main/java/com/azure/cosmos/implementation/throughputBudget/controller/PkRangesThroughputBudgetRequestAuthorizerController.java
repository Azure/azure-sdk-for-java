// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget.controller;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.throughputBudget.ThroughputBudgetRequestAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PkRangesThroughputBudgetRequestAuthorizerController implements ThroughputBudgetRequestAuthorizerController {
    private final static Logger logger = LoggerFactory.getLogger(PkRangesThroughputBudgetRequestAuthorizerController.class);
    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<String>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final ConcurrentHashMap<String, Pair<PartitionKeyRange, ThroughputBudgetRequestAuthorizer>> requestAuthorizerList;
    private final String resolvedContainerRid;


    public PkRangesThroughputBudgetRequestAuthorizerController(
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String resolvedContainerRid) {

        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.requestAuthorizerList = new ConcurrentHashMap<>();
        this.resolvedContainerRid = resolvedContainerRid;
    }

    @Override
    public Mono<Void> resetThroughput(double scheduledThroughput) {
        double throughputPerPkRange = this.calculateThroughputPerPkRange(scheduledThroughput, this.requestAuthorizerList.size());
        return Flux.fromIterable(this.requestAuthorizerList.values())
            .flatMap(pkRangesRequestHandler ->
            {
                logger.info(pkRangesRequestHandler.getLeft().getId() + " : reset throughput");
                return pkRangesRequestHandler.getRight().resetThroughput(throughputPerPkRange);
            })
            .then();
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        PartitionKeyRange resolvedPkRange = request.requestContext.resolvedPartitionKeyRange;
        return this.requestAuthorizerList.containsKey(resolvedPkRange.getId())
            || (resolvedPkRange.getParents() != null
                    && resolvedPkRange.getParents().stream().anyMatch(parentPkRangeId -> this.requestAuthorizerList.containsKey(parentPkRangeId)));
    }

    @Override
    public Mono<Void> close() {
        return Mono.empty();
    }

    @Override
    public Mono<ThroughputBudgetRequestAuthorizerController> init(double scheduledThroughput) {
        return this.getPartitionKeyRanges(RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES)
            .flatMapMany(pkRanges -> {
                double throughputPerPkRange = this.calculateThroughputPerPkRange(scheduledThroughput, pkRanges.size());
                return Flux.fromIterable(pkRanges)
                    .map(pkRange -> Pair.of(pkRange, throughputPerPkRange));
            })
            .flatMap(pkRangePair -> {
                ThroughputBudgetRequestAuthorizer requestAuthorizer = new ThroughputBudgetRequestAuthorizer(pkRangePair.getRight());
                requestAuthorizer.resetThroughput(pkRangePair.getRight());
                this.requestAuthorizerList.put(pkRangePair.getLeft().getId(), Pair.of(pkRangePair.getLeft(), requestAuthorizer));

                return Mono.empty();
            })
            .then(Mono.just(this));
    }

    @Override
    public Mono<Double> calculateLoadFactor() {
        // Each time only capture the largest one
        return Flux.fromIterable(this.requestAuthorizerList.values())
            .map(pair -> pair.getRight())
            .flatMap(requestAuthorizer -> requestAuthorizer.calculateLoadFactor())
            .collectList()
            .flatMap(loadFactorList -> Mono.just(Collections.max(loadFactorList)));
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        PartitionKeyRange resolvedPkRange = request.requestContext.resolvedPartitionKeyRange;

        return Mono.justOrEmpty(this.requestAuthorizerList.get(resolvedPkRange.getId()))
            .switchIfEmpty(
                Mono.justOrEmpty(resolvedPkRange.getParents())
                    .flatMap(parentPkRangeId -> {
                        return Flux.fromIterable(() -> this.requestAuthorizerList.keys().asIterator())
                            .filter(pkRangeId -> pkRangeId.equals(parentPkRangeId))
                            .flatMap(pkRangeId -> this.handleSplit(this.requestAuthorizerList.get(pkRangeId).getLeft(), this.requestAuthorizerList.get(pkRangeId).getRight()))
                            .then(Mono.justOrEmpty(this.requestAuthorizerList.get(resolvedPkRange)));
                    })
            )
            .flatMap(pair -> {
                return pair.getRight().authorizeRequest(request, nextRequestMono)
                    .doOnError(throwable -> this.doOnError(pair.getLeft(), pair.getRight(), throwable));
            })
            .switchIfEmpty(nextRequestMono);
    }

    private Mono<Void> addChildRequestAuthorizerForSplit(ThroughputBudgetRequestAuthorizer parent, List<PartitionKeyRange> childRanges) {

        checkNotNull(parent, "Parent request authroizer cannot be null");
        checkArgument((childRanges != null && childRanges.size() > 0), "Child partition key ranges can not be null or empty");

        double scheduledThroughputPerChild = parent.getScheduledThoughput() / childRanges.size();
        double availabeThroughputPerChild = parent.getAvailableThroughput() / childRanges.size();
        int totalRequestsPerChild = parent.getTotalRequests() / childRanges.size();
        int rejectedRequestsPerChild = parent.getRejectedRequests() / childRanges.size();

        return Flux.fromIterable(childRanges)
            .flatMap(pkRange -> {
                this.requestAuthorizerList.compute(pkRange.getId(), (pkRangeId, requestAuthroizer) -> {
                    if (requestAuthroizer == null) {
                        ThroughputBudgetRequestAuthorizer childAuthorizer = new ThroughputBudgetRequestAuthorizer(scheduledThroughputPerChild);
                        childAuthorizer.setAvailableThroughput(availabeThroughputPerChild);
                        childAuthorizer.setRejectedRequests(rejectedRequestsPerChild);
                        childAuthorizer.setTotalRequests(totalRequestsPerChild);

                        return Pair.of(pkRange, new ThroughputBudgetRequestAuthorizer(availabeThroughputPerChild));
                    }
                    // the child pkRangeHandler may already added by another thread
                    return requestAuthroizer;
                });
                return Mono.empty();
            }).then();
    }

    private double calculateThroughputPerPkRange(double scheduledThroughput, int ranges) {
        return scheduledThroughput / ranges;
    }

    private Mono<Void> doOnError(PartitionKeyRange pkRange, ThroughputBudgetRequestAuthorizer requestAuthorizer, Throwable throwable) {
        checkNotNull(throwable, "Exception cannot be null");
        CosmosException cosmosException = Utils.as(throwable, CosmosException.class);

        // TODO: shoudl also check completing split here?
        if (cosmosException != null &&
            Exceptions.isStatusCode(cosmosException, HttpConstants.StatusCodes.GONE) &&
            Exceptions.isSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE)) {

            return this.handleSplit(pkRange, requestAuthorizer);
        }

        return Mono.empty();
    }

    private Mono<List<PartitionKeyRange>> getPartitionKeyRanges(Range<String> range) {
        checkNotNull(range, "Range can not be null");
        // TODO: add diagnostics context
        return this.partitionKeyRangeCache
            .tryGetOverlappingRangesAsync(null, this.resolvedContainerRid, range, true, null)
            .map(partitionKeyRangesValueHolder -> partitionKeyRangesValueHolder.v);
    }

    private Mono<Void> handleSplit(PartitionKeyRange pkRange, ThroughputBudgetRequestAuthorizer requestAuthorizer) {
        return this.getPartitionKeyRanges(pkRange.toRange())
            .flatMap(pkRanges -> {
                if (pkRanges.size() > 0) {
                    return this.addChildRequestAuthorizerForSplit(requestAuthorizer, pkRanges);
                }

                return Mono.empty();
            })
            .doOnSuccess(avoid -> this.requestAuthorizerList.remove(pkRange.getId()));
    }
}
