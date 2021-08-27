// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

// We suppress the "try" warning here because the close() method's signature
// allows it to throw InterruptedException which is strongly advised against
// by AutoCloseable (see: http://docs.oracle.com/javase/7/docs/api/java/lang/AutoCloseable.html#close()).
// close() will never throw an InterruptedException but the exception remains in the
// signature for backwards compatibility purposes.
@SuppressWarnings("try")
public abstract class TransportClient implements AutoCloseable {
    private ThroughputControlStore throughputControlStore;

    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
        this.throughputControlStore = throughputControlStore;
    }

    // Uses requests's ResourceOperation to determine the operation
    public Mono<StoreResponse> invokeResourceOperationAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
        if (StringUtils.isEmpty(request.requestContext.resourcePhysicalAddress)) {
            request.requestContext.resourcePhysicalAddress = physicalAddress.toString();
        }
        if (this.throughputControlStore != null) {
            return this.throughputControlStore.processRequest(
                request,
                Mono.defer(
                    () -> this.invokeStoreAsync(physicalAddress, request).publishOn(CosmosSchedulers.TRANSPORT_CLIENT_BOUNDED_ELASTIC)));
        }

        return this.invokeStoreAsync(physicalAddress, request)
            .publishOn(CosmosSchedulers.TRANSPORT_CLIENT_BOUNDED_ELASTIC);
    }

    protected abstract Mono<StoreResponse> invokeStoreAsync(
        Uri physicalAddress,
        RxDocumentServiceRequest request);
}
