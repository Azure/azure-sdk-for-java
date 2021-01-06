// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.request;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestAuthorizer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PkRangesThroughputRequestController implements IThroughputRequestController {
    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<String>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final ConcurrentHashMap<String, ThroughputRequestAuthorizer> requestAuthorizerMap;
    private final String targetContainerRid;
    private final double initialScheduledThroughput;

    public PkRangesThroughputRequestController(
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid,
        double initialScheduledThroughput) {

        checkNotNull(partitionKeyRangeCache, "RxPartitionKeyRangeCache can not be null");
        checkArgument(StringUtils.isNotEmpty(targetContainerRid), "Target container rid can not be null nor empty");

        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.requestAuthorizerMap = new ConcurrentHashMap<>();
        this.targetContainerRid = targetContainerRid;
        this.initialScheduledThroughput = initialScheduledThroughput;
    }

    @Override
    public Mono<Void> renewThroughputUsageCycle(double scheduledThroughput) {
        double throughputPerPkRange = this.calculateThroughputPerPkRange(scheduledThroughput, this.requestAuthorizerMap.size());
        return Flux.fromIterable(this.requestAuthorizerMap.values())
            .flatMap(requestAuthorizer -> requestAuthorizer.renewThroughputUsageCycle(throughputPerPkRange))
            .then();
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        PartitionKeyRange resolvedPkRange = request.requestContext.resolvedPartitionKeyRange;
        if (resolvedPkRange != null) {
            return this.requestAuthorizerMap.containsKey(resolvedPkRange.getId());
        }

        return false;
    }

    @Override
    public Mono<Void> close() {
        return Mono.empty();
    }

    @Override
    public Mono<IThroughputRequestController> init() {
        return this.getPartitionKeyRanges(RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES)
            .flatMapMany(pkRanges -> {
                if (pkRanges == null || pkRanges.isEmpty()) {
                    return Mono.empty();
                }

                double throughputPerPkRange = this.calculateThroughputPerPkRange(this.initialScheduledThroughput, pkRanges.size());
                return Flux.fromIterable(pkRanges)
                    .flatMap(pkRange -> {
                        return this.createAndInitializeRequestAuthorizer(throughputPerPkRange)
                            .doOnSuccess(requestAuthorizer -> this.requestAuthorizerMap.put(pkRange.getId(), requestAuthorizer));
                    });
            })
            .then(Mono.just(this));
    }

    private Mono<ThroughputRequestAuthorizer> createAndInitializeRequestAuthorizer(double throughputPerPkRange) {
        return Mono.just(new ThroughputRequestAuthorizer(throughputPerPkRange))
            .flatMap(requestAuthorizer -> requestAuthorizer.init());
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        PartitionKeyRange resolvedPkRange = request.requestContext.resolvedPartitionKeyRange;
        return Mono.just(this.requestAuthorizerMap.get(resolvedPkRange.getId()))
            .flatMap(requestAuthorizer -> {
                if (requestAuthorizer != null) {
                    return requestAuthorizer.authorize(nextRequestMono);
                } else {
                    return nextRequestMono;
                }
            });
    }

    private double calculateThroughputPerPkRange(double scheduledThroughput, int pkRangeCount) {
        checkArgument(pkRangeCount > 0, "Pk range count can not be 0");
        return scheduledThroughput / pkRangeCount;
    }

    private Mono<List<PartitionKeyRange>> getPartitionKeyRanges(Range<String> range) {
        checkNotNull(range, "Range can not be null");
        // TODO: add diagnostics context
        return this.partitionKeyRangeCache
            .tryGetOverlappingRangesAsync(null, this.targetContainerRid, range, true, null)
            .map(partitionKeyRangesValueHolder -> partitionKeyRangesValueHolder.v);
    }
}
