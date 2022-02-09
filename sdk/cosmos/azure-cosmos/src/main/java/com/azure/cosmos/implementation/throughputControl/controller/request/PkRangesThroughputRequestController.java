// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.request;

import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestThrottler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PkRangesThroughputRequestController implements IThroughputRequestController {
    private static final Logger logger = LoggerFactory.getLogger(PkRangesThroughputRequestController.class);
    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<String>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private final RxPartitionKeyRangeCache partitionKeyRangeCache;
    private final ConcurrentHashMap<String, ThroughputRequestThrottler> requestThrottlerMap;
    private final String targetContainerRid;
    private double scheduledThroughput;
    private List<PartitionKeyRange> pkRanges;

    public PkRangesThroughputRequestController(
        RxPartitionKeyRangeCache partitionKeyRangeCache,
        String targetContainerRid,
        double initialScheduledThroughput) {

        checkNotNull(partitionKeyRangeCache, "RxPartitionKeyRangeCache can not be null");
        checkArgument(StringUtils.isNotEmpty(targetContainerRid), "Target container rid can not be null nor empty");

        this.partitionKeyRangeCache = partitionKeyRangeCache;
        this.requestThrottlerMap = new ConcurrentHashMap<>();
        this.targetContainerRid = targetContainerRid;
        this.scheduledThroughput = initialScheduledThroughput;
    }

    @Override
    public double renewThroughputUsageCycle(double scheduledThroughput) {
        this.scheduledThroughput = scheduledThroughput;
        double throughputPerPkRange = this.calculateThroughputPerPkRange();
        return this.requestThrottlerMap.values()
            .stream()
            .map(requestThrottler -> requestThrottler.renewThroughputUsageCycle(throughputPerPkRange))
            .max(Comparator.naturalOrder())// return the max throughput usage among all regions
            .get();
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
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return this.getPartitionKeyRanges(RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES)
            .doOnSuccess(pkRanges -> {
                this.pkRanges = pkRanges;
                this.createRequestThrottlers();
            })
            .then(Mono.just((T)this));
    }

    private void createRequestThrottlers() {
        double throughputPerPkRange = this.calculateThroughputPerPkRange();

        for (PartitionKeyRange pkRange : pkRanges) {
            requestThrottlerMap.compute(pkRange.getId(), (pkRangeId, requestThrottler) -> {
                if (requestThrottler == null) {
                    requestThrottler = new ThroughputRequestThrottler(throughputPerPkRange, pkRangeId);
                }

                return requestThrottler;
            });
        }
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        PartitionKeyRange resolvedPkRange = request.requestContext.resolvedPartitionKeyRange;

        // If we reach here, it means we should find the mapping pkRange
        ThroughputRequestThrottler requestThrottler = this.requestThrottlerMap.get(resolvedPkRange.getId());

        if (requestThrottler != null) {
            return requestThrottler.processRequest(request, nextRequestMono);
        } else {
            logger.warn(
                "Can not find matching request throttler to process request {} with pkRangeId {}",
                request.getActivityId(),
                resolvedPkRange.getId());
            return nextRequestMono;
        }
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
