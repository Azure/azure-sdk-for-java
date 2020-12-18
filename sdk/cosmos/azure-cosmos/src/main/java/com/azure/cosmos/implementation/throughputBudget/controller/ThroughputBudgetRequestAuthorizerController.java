// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputBudget.controller;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import reactor.core.publisher.Mono;

public interface ThroughputBudgetRequestAuthorizerController extends IThroughputBudgetController {
    Mono<Void> resetThroughput(double throughput);
    boolean canHandleRequest(RxDocumentServiceRequest request);
    Mono<ThroughputBudgetRequestAuthorizerController> init(double scheduledThroughput);
    Mono<Double> calculateLoadFactor();
}
