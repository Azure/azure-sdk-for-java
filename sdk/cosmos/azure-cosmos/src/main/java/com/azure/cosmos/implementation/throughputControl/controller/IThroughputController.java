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
     * @param request The request.
     * @return Flag to indicate whether the controller can handle the request. Each controller will have its onw criteria.
     */
    boolean canHandleRequest(RxDocumentServiceRequest request);

    /**
     * Initialize process.
     * Will create and initialize the lower level throughput controller and schedule tasks if needed.
     * @return The initialized controller.
     */
    <T> Mono<T> init();

    /**
     * Route the request to lower level throughput controller which can handle the request.
     * @param request
     * @param originalRequestMono
     * @return The response from the original request mono.
     */
    <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> originalRequestMono);
}
