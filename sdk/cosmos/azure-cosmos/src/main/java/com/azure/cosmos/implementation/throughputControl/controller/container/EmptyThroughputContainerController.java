// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.container;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import reactor.core.publisher.Mono;

/**
 * This is a dummy throughput container controller, which will just let the requests pass by.
 * This will be created when there is no throughput control group defined for the container.
 */
public class EmptyThroughputContainerController implements IThroughputContainerController {
    @Override
    public boolean canHandleRequest(RxDocumentServiceRequest request) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Mono<T> init() {
        return (Mono<T>) Mono.just(this);
    }

    @Override
    public <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono) {
        return nextRequestMono;
    }
}
