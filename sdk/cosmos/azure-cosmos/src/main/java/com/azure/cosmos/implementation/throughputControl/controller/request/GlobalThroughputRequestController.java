// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.request;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestThrottler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GlobalThroughputRequestController implements IThroughputRequestController {
    private final GlobalEndpointManager globalEndpointManager;
    private final AtomicReference<Double> scheduledThroughput;
    private final ConcurrentHashMap<URI, ThroughputRequestThrottler> requestThrottlerMapByRegion;

    public GlobalThroughputRequestController(GlobalEndpointManager globalEndpointManager, double initialScheduledThroughput) {
        checkNotNull(globalEndpointManager, "Global endpoint manager can not be null");

        this.globalEndpointManager = globalEndpointManager;
        this.scheduledThroughput = new AtomicReference<>(initialScheduledThroughput);
        this.requestThrottlerMapByRegion = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return Flux.fromIterable(this.globalEndpointManager.getReadEndpoints())
            .flatMap(endpoint -> {
                requestThrottlerMapByRegion.computeIfAbsent(endpoint, key -> new ThroughputRequestThrottler(this.scheduledThroughput.get()));
                return Mono.empty();
            })
            .then(Mono.just((T)this));
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return true;
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        return Mono.defer(
                () -> Mono.just(
                    this.requestThrottlerMapByRegion.computeIfAbsent(
                        this.globalEndpointManager.resolveServiceEndpoint(request),
                        key -> new ThroughputRequestThrottler(this.scheduledThroughput.get())))
            )
            .flatMap(requestThrottler -> requestThrottler.processRequest(request, nextRequestMono));
    }

    @Override
    public void renewThroughputUsageCycle(double throughput) {
        this.scheduledThroughput.set(throughput);
        this.requestThrottlerMapByRegion.values()
            .stream()
            .forEach(requestThrottler -> requestThrottler.renewThroughputUsageCycle(throughput));
    }

    @Override
    public Mono<Void> close() {
        return Mono.empty();
    }
}
