// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.models.CosmosContainerIdentity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Used internally to provide functionality to communicate and process response from THINCLIENT in the Azure Cosmos DB database service.
 */
public class RxThinclientStoreModel implements RxStoreModel {
    @Override
    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        return null;
    }

    @Override
    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {

    }

    @Override
    public Flux<Void> submitOpenConnectionTasksAndInitCaches(CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {
        return null;
    }

    @Override
    public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider, Configs configs) {

    }

    @Override
    public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {

    }

    @Override
    public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {

    }
}
