// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import reactor.core.publisher.Mono;

public interface IThroughputController {
    /**
     * Decides whether the throughputController can handle the request.
     * Different level throughput controller will have its own criteria.
     *
     * @param request
     * @return
     */
    boolean canHandleRequest(RxDocumentServiceRequest request);

    /**
     * Close all the scheduled tasks and any other resources need to release.
     * @return
     */
    Mono<Void> close();

    /**
     * Initialize process.
     * Will create and initialize the lower level throughput controller and schedule tasks if needed.
     * @return
     */
    <T> Mono<T> init();

    /**
     * Route the request to lower level throughput controller which can handle the request.
     * @param request
     * @param nextRequestMono
     * @return
     */
    <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono);
}
