// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import reactor.core.publisher.Mono;

import java.net.URI;

public abstract class TransportClient implements AutoCloseable {

    // Uses requests's ResourceOperation to determine the operation
    public Mono<StoreResponse> invokeResourceOperationAsync(URI physicalAddress, RxDocumentServiceRequest request) {
        return this.invokeStoreAsync(physicalAddress, request);
    }

    protected abstract Mono<StoreResponse> invokeStoreAsync(
        URI physicalAddress,
        RxDocumentServiceRequest request);
}
