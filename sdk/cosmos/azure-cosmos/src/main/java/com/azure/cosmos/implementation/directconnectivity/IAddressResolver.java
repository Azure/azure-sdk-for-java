// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.models.OpenConnectionAggressivenessHint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAddressResolver {

    Mono<AddressInformation[]> resolveAsync(
            RxDocumentServiceRequest request,
            boolean forceRefreshPartitionAddresses);

    /**
     * Warm up caches and open connections for containers specified by
     * {@link CosmosContainerProactiveInitConfig#getCosmosContainerIdentities()} to replicas in
     * {@link CosmosContainerProactiveInitConfig#getProactiveConnectionRegionsCount()} preferred regions.
     *
     * @param proactiveContainerInitConfig the instance encapsulating a list of container identities and no. of proactive connection regions
     * @return A flux of {@link OpenConnectionResponse}.
     */
    Flux<Void> submitOpenConnectionTasksAndInitCaches(
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig,
            OpenConnectionAggressivenessHint hint
    );

    /***
     * Set the open connection handler so SDK can proactively create connections based on need.
     *
     * @param openConnectionHandler the {@link IOpenConnectionsHandler}.
     */
    void setOpenConnectionsHandler(IOpenConnectionsHandler openConnectionHandler);

    void setOpenConnectionsProcessor(ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor);
}
