// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.interceptor.ITransportClientInterceptor;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.models.CosmosContainerIdentity;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

// We suppress the "try" warning here because the close() method's signature
// allows it to throw InterruptedException which is strongly advised against
// by AutoCloseable (see: http://docs.oracle.com/javase/7/docs/api/java/lang/AutoCloseable.html#close()).
// close() will never throw an InterruptedException but the exception remains in the
// signature for backwards compatibility purposes.
@SuppressWarnings("try")
public abstract class TransportClient implements AutoCloseable {
    private final boolean switchOffIOThreadForResponse = Configs.shouldSwitchOffIOThreadForResponse();
    private ThroughputControlStore throughputControlStore;
    private List<ITransportClientInterceptor> transportClientInterceptors;

    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
        this.throughputControlStore = throughputControlStore;
    }

    public Mono<StoreResponse> invokeResourceOperationAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
        return invokeResourceOperationInternalAsync(physicalAddress, request)
            .map(response -> {
                if (this.transportClientInterceptors == null) {
                    return response;
                } else {
                    // there are interceptors configured, process the store response with the interceptors
                    StoreResponse storeResponse = response;
                    for (ITransportClientInterceptor transportClientInterceptor : this.transportClientInterceptors) {
                        if(transportClientInterceptor.getStoreResponseInterceptor() != null) {
                            storeResponse = transportClientInterceptor.getStoreResponseInterceptor().apply(request, storeResponse);
                        }
                    }

                    return storeResponse;
                }
            });
    }

    // Uses request's ResourceOperation to determine the operation
    public Mono<StoreResponse> invokeResourceOperationInternalAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
        if (StringUtils.isEmpty(request.requestContext.resourcePhysicalAddress)) {
            request.requestContext.resourcePhysicalAddress = physicalAddress.toString();
        }

        if (this.throughputControlStore != null) {
            return this.invokeStoreWithThroughputControlAsync(physicalAddress, request);
        }

        return this.invokeStoreInternalAsync(physicalAddress, request);
    }

    protected abstract Mono<StoreResponse> invokeStoreAsync(
        Uri physicalAddress,
        RxDocumentServiceRequest request);

    public abstract void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider);

    public abstract GlobalEndpointManager getGlobalEndpointManager();

    public abstract ProactiveOpenConnectionsProcessor getProactiveOpenConnectionsProcessor();

    public abstract void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities);

    public abstract void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities);

    public synchronized void registerTransportClientInterceptor(ITransportClientInterceptor transportClientInterceptor) {
        if (this.transportClientInterceptors == null) {
            this.transportClientInterceptors = new ArrayList<>();
        }

        this.transportClientInterceptors.add(transportClientInterceptor);
    }

    private Mono<StoreResponse> invokeStoreWithThroughputControlAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
        return this.throughputControlStore.processRequest(
            request,
            Mono.defer(() -> this.invokeStoreInternalAsync(physicalAddress, request)));
    }

    private Mono<StoreResponse> invokeStoreInternalAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
        if (switchOffIOThreadForResponse) {
            return this.invokeStoreAsync(physicalAddress, request).publishOn(CosmosSchedulers.TRANSPORT_RESPONSE_BOUNDED_ELASTIC);
        }

        return this.invokeStoreAsync(physicalAddress, request);
    }
}
