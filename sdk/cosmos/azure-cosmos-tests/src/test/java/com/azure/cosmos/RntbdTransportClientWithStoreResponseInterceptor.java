// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.RntbdTransportClient;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.models.CosmosContainerIdentity;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiFunction;

public class RntbdTransportClientWithStoreResponseInterceptor extends RntbdTransportClient {
    private final RntbdTransportClient underlying;
    private BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> responseInterceptor;

    public RntbdTransportClientWithStoreResponseInterceptor(RntbdTransportClient underlying) {
        super(underlying);
        this.underlying = underlying;
    }

    public void setResponseInterceptor(BiFunction<RxDocumentServiceRequest, StoreResponse, StoreResponse> responseInterceptor) {
        this.responseInterceptor = responseInterceptor;
    }

    @Override
    public Mono<StoreResponse> invokeStoreAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
        return this.underlying.invokeStoreAsync(physicalAddress, request)
            .map(response -> {
                if (responseInterceptor != null) {
                    return responseInterceptor.apply(request, response);
                }
                return response;
            });
    }

    @Override
    public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider) {
        this.underlying.configureFaultInjectorProvider(injectorProvider);
    }

    @Override
    public GlobalEndpointManager getGlobalEndpointManager() {
        return this.underlying.getGlobalEndpointManager();
    }

    @Override
    public ProactiveOpenConnectionsProcessor getProactiveOpenConnectionsProcessor() {
        return this.underlying.getProactiveOpenConnectionsProcessor();
    }

    @Override
    public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.underlying.recordOpenConnectionsAndInitCachesCompleted(cosmosContainerIdentities);
    }

    @Override
    public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.underlying.recordOpenConnectionsAndInitCachesStarted(cosmosContainerIdentities);
    }

    @Override
    public void close() {
        this.underlying.close();
    }
}
