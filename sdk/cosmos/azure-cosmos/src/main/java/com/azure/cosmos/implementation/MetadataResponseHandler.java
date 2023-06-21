// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import reactor.core.publisher.Mono;

import java.net.URI;

public class MetadataResponseHandler {

    private final GlobalEndpointManager globalEndpointManager;

    public MetadataResponseHandler(GlobalEndpointManager globalEndpointManager) {
        this.globalEndpointManager = globalEndpointManager;
    }

    public Mono<Void> markRegionAsUnavailable(RxDocumentServiceRequest request, CosmosException cosmosException) {

        URI locationEndpointToRoute = request.requestContext.locationEndpointToRoute;

        if (Exceptions.isSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE)) {
            if (request.isReadOnlyRequest()) {
                this.globalEndpointManager.markEndpointUnavailableForRead(locationEndpointToRoute);
            } else {
                this.globalEndpointManager.markEndpointUnavailableForWrite(locationEndpointToRoute);
            }
        }

        if (Exceptions.isSubStatusCode(cosmosException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT)) {
            if (request.isReadOnlyRequest()) {
                this.globalEndpointManager.markEndpointUnavailableForRead(locationEndpointToRoute);
            } else {
                this.globalEndpointManager.markEndpointUnavailableForWrite(locationEndpointToRoute);
            }
        }

        return Mono.empty();
    }

}
