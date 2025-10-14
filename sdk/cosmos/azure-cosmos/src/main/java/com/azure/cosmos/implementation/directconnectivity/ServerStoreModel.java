// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;


import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.RxStoreModel;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.models.CosmosContainerIdentity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ServerStoreModel implements RxStoreModel {
    private final StoreClient storeClient;

    public ServerStoreModel(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        String requestConsistencyLevelHeaderValue =
            request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);
        String requestReadConsistencyStrategyHeaderValue =
            request.getHeaders().get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY);

        request.requestContext.originalRequestConsistencyLevel = null;
        request.requestContext.readConsistencyStrategy = ReadConsistencyStrategy.DEFAULT;

        if (!Strings.isNullOrEmpty(requestConsistencyLevelHeaderValue)) {
            ConsistencyLevel requestConsistencyLevel;
            if ((requestConsistencyLevel = BridgeInternal.fromServiceSerializedFormat(requestConsistencyLevelHeaderValue)) == null) {
                return Mono.error(new BadRequestException(
                    String.format(
                        RMResources.InvalidHeaderValue,
                        requestConsistencyLevelHeaderValue,
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)));
            }

            request.requestContext.originalRequestConsistencyLevel = requestConsistencyLevel;
        }

        if (!Strings.isNullOrEmpty(requestReadConsistencyStrategyHeaderValue)) {
            ReadConsistencyStrategy requestReadConsistencyStrategy;
            if ((requestReadConsistencyStrategy = ImplementationBridgeHelpers
                .ReadConsistencyStrategyHelper
                .getReadConsistencyStrategyAccessor()
                .createFromServiceSerializedFormat(requestReadConsistencyStrategyHeaderValue)) == null) {

                return Mono.error(new BadRequestException(
                    String.format(
                        RMResources.InvalidHeaderValue,
                        requestConsistencyLevelHeaderValue,
                        HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)));
            }

            request.requestContext.readConsistencyStrategy = requestReadConsistencyStrategy;
        }

        if (ReplicatedResourceClient.isMasterResource(request.getResourceType())) {
            request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.STRONG.toString());
        }

        return this.storeClient.processMessageAsync(request);
    }

    @Override
    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
        this.storeClient.enableThroughputControl(throughputControlStore);
    }

    @Override
    public Flux<Void> submitOpenConnectionTasksAndInitCaches(CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {
        return this.storeClient.submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig);
    }

    @Override
    public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider, Configs configs) {
        this.storeClient.configureFaultInjectorProvider(injectorProvider);
    }

    @Override
    public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.storeClient.recordOpenConnectionsAndInitCachesCompleted(cosmosContainerIdentities);
    }

    public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        this.storeClient.recordOpenConnectionsAndInitCachesStarted(cosmosContainerIdentities);
    }
}
