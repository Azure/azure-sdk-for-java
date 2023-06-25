// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;

import java.net.URI;

public class MetadataResponseHandler {

    private final GlobalEndpointManager globalEndpointManager;

    public MetadataResponseHandler(GlobalEndpointManager globalEndpointManager) {
        this.globalEndpointManager = globalEndpointManager;
    }

    public void attemptToMarkRegionAsUnavailable(RxDocumentServiceRequest request, CosmosException cosmosException) {

        URI locationEndpointToRoute = request.requestContext.locationEndpointToRoute;

        if (!request.isAddressRefresh()) {
            return;
        }

        if (shouldMarkRegionAsUnavailable(cosmosException)) {
            if (request.isReadOnlyRequest()) {
                this.globalEndpointManager.markEndpointUnavailableForRead(locationEndpointToRoute);
            } else {
                this.globalEndpointManager.markEndpointUnavailableForWrite(locationEndpointToRoute);
            }
        }
    }

    private static boolean shouldMarkRegionAsUnavailable(CosmosException exception) {

        // check for network issues or connectivity issues
        if (WebExceptionUtility.isNetworkFailure(exception) || WebExceptionUtility.isConnectionException(exception)) {
            return Exceptions.isSubStatusCode(exception, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE) ||
                Exceptions.isSubStatusCode(exception, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
        }

        return false;
    }
}
