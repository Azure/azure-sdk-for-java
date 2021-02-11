// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.request;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestThrottler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PkRangesThroughputRequestController implements IThroughputRequestController {
    private static final Logger logger = LoggerFactory.getLogger(PkRangesThroughputRequestController.class);
    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<String>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private final GlobalEndpointManager globalEndpointManager;
    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final ConcurrentHashMap<URI, ConcurrentHashMap<String, ThroughputRequestThrottler>> requestThrottlerMapByRegion;
    private final String targetContainerRid;
    private double scheduledThroughput;
    private List<PartitionKeyRange> pkRanges;

    public PkRangesThroughputRequestController(
        GlobalEndpointManager globalEndpointManager,
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid,
        double initialScheduledThroughput) {

        checkNotNull(globalEndpointManager, "Global endpoint manager can not be null");
        checkNotNull(partitionKeyRangeCache, "RxPartitionKeyRangeCache can not be null");
        checkArgument(StringUtils.isNotEmpty(targetContainerRid), "Target container rid can not be null nor empty");

        this.globalEndpointManager = globalEndpointManager;
        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.requestThrottlerMapByRegion = new ConcurrentHashMap<>();
        this.targetContainerRid = targetContainerRid;
        this.scheduledThroughput = initialScheduledThroughput;
    }

    @Override
    public void renewThroughputUsageCycle(double scheduledThroughput) {
        this.scheduledThroughput = scheduledThroughput;
        double throughputPerPkRange = this.calculateThroughputPerPkRange();
        this.requestThrottlerMapByRegion.values()
            .forEach(requestThrottlerMapByRegion -> {
                requestThrottlerMapByRegion
                    .values()
                    .forEach(requestThrottler -> requestThrottler.renewThroughputUsageCycle(throughputPerPkRange));
            });
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        if (request.requestContext != null) {
            PartitionKeyRange resolvedPkRange = request.requestContext.resolvedPartitionKeyRange;
            if (resolvedPkRange != null) {
                return this.pkRanges.contains(resolvedPkRange);
            }
        }

        return false;
    }

    @Override
    public Mono<Void> close() {
        return Mono.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.getPartitionKeyRanges(RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES)
            .flatMap(pkRanges -> {
                this.pkRanges = pkRanges;
                return this.createRequestThrottlers();
            })
            .then(Mono.just((T)this));
    }

    private Mono<Void> createRequestThrottlers() {
        // create request throttlers by region
        return Flux.fromIterable(this.globalEndpointManager.getReadEndpoints())
            .flatMap(endpoint -> this.getOrCreateRegionRequestThrottlers(endpoint))
            .then();
    }

    private Mono<ConcurrentHashMap<String, ThroughputRequestThrottler>> getOrCreateRegionRequestThrottlers(URI endpoint) {
        double throughputPerPkRange = this.calculateThroughputPerPkRange();
        return Mono.just(this.requestThrottlerMapByRegion.computeIfAbsent(endpoint, key -> {
            ConcurrentHashMap<String, ThroughputRequestThrottler> requestThrottlerPerPkRange =
                new ConcurrentHashMap<>();

            for (PartitionKeyRange pkRange : pkRanges) {
                requestThrottlerPerPkRange.put(pkRange.getId(), new ThroughputRequestThrottler(throughputPerPkRange));
            }

            return requestThrottlerPerPkRange;
        }));
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        PartitionKeyRange resolvedPkRange = request.requestContext.resolvedPartitionKeyRange;

        // If we reach here, it means we should find the mapping pkRange
        return this.getOrCreateRegionRequestThrottlers(this.globalEndpointManager.resolveServiceEndpoint(request))
            .flatMap(regionRequestThrottlers -> Mono.just(regionRequestThrottlers.get(resolvedPkRange.getId())))
            .flatMap(requestThrottler -> {
                if (requestThrottler != null) {
                    return requestThrottler.processRequest(request, nextRequestMono);
                } else {
                    logger.warn(
                        "Can not find matching request throttler to process request {} with pkRangeId {}",
                        request.getActivityId(),
                        resolvedPkRange.getId());
                    return nextRequestMono;
                }
            });
    }

    private double calculateThroughputPerPkRange() {
        checkArgument(this.pkRanges != null && this.pkRanges.size() > 0, "Pk range count can not be 0");
        return this.scheduledThroughput / this.pkRanges.size();
    }

    private Mono<List<PartitionKeyRange>> getPartitionKeyRanges(Range<String> range) {
        checkNotNull(range, "Range can not be null");
        // TODO: add diagnostics context
        return this.partitionKeyRangeCache
            .tryGetOverlappingRangesAsync(null, this.targetContainerRid, range, true, null)
            .map(partitionKeyRangesValueHolder -> partitionKeyRangesValueHolder.v);
    }
}
