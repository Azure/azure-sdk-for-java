// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.request;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestAuthorizer;
import reactor.core.publisher.Mono;

public class GlobalThroughputRequestController implements IThroughputRequestController {
    private final double initialScheduledThroughput;
    private ThroughputRequestAuthorizer requestAuthorizer;

    public GlobalThroughputRequestController(double scheduledThroughput) {
        this.initialScheduledThroughput = scheduledThroughput;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        this.requestAuthorizer= new ThroughputRequestAuthorizer(this.initialScheduledThroughput);
        return Mono.just((T)this);
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return true;
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        return this.requestAuthorizer.authorize(request, nextRequestMono);
    }

    @Override
    public Mono<Void> renewThroughputUsageCycle(double throughput) {
        return Mono.fromRunnable(() -> this.requestAuthorizer.renewThroughputUsageCycle(throughput)).then();
    }

    @Override
    public Mono<Void> close() {
        return Mono.empty();
    }
}
