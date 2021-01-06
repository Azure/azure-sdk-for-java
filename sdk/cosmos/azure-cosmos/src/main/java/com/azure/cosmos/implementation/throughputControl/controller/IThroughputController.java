// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import reactor.core.publisher.Mono;

public interface IThroughputController {
    boolean canHandleRequest(RxDocumentServiceRequest request);
    Mono<Void> close();
    <T> Mono<T> init();
    <T> Mono<T> processRequest(RxDocumentServiceRequest request, Mono<T> nextRequestMono);
}
