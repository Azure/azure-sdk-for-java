// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.request;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestThrottler;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

public class GlobalThroughputRequestController implements IThroughputRequestController {
    private final AtomicReference<Double> scheduledThroughput;
    private final ThroughputRequestThrottler requestThrottler;

    public GlobalThroughputRequestController(double initialScheduledThroughput) {
        this.scheduledThroughput = new AtomicReference<>(initialScheduledThroughput);
        this.requestThrottler = new ThroughputRequestThrottler(this.scheduledThroughput.get(), StringUtils.EMPTY);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return Mono.just((T)requestThrottler);
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return true;
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono) {
        return this.requestThrottler.processRequest(request, originalRequestMono);
    }

    @Override
    public double renewThroughputUsageCycle(double throughput) {
        this.scheduledThroughput.set(throughput);
        return this.requestThrottler.renewThroughputUsageCycle(throughput);
    }
}
