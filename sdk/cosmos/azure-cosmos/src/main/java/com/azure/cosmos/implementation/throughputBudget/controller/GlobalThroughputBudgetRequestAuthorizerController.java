// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget.controller;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.throughputBudget.ThroughputBudgetRequestAuthorizer;
import reactor.core.publisher.Mono;

public class GlobalThroughputBudgetRequestAuthorizerController implements ThroughputBudgetRequestAuthorizerController {
    private ThroughputBudgetRequestAuthorizer requestAuthorizer;

    @Override
    public Mono<Void> resetThroughput(double throughput) {
        return Mono.fromRunnable(() -> this.requestAuthorizer.resetThroughput(throughput))
            .then();
    }

    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return true;
    }

    @Override
    public Mono<ThroughputBudgetRequestAuthorizerController> init(double scheduledThroughput) {
        this.requestAuthorizer= new ThroughputBudgetRequestAuthorizer(scheduledThroughput);
        return Mono.just(this);
    }

    @Override
    public Mono<Double> calculateLoadFactor() {
        return requestAuthorizer.calculateLoadFactor();
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        return this.requestAuthorizer.authorizeRequest(request, nextRequestMono);
    }

    @Override
    public Mono<Void> close() {
        return Mono.empty();
    }
}
