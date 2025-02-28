// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;

public class MetadataRequestRetryPolicy implements IRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(MetadataRequestRetryPolicy.class);
    private final GlobalEndpointManager globalEndpointManager;
    private RxDocumentServiceRequest request;
    private WebExceptionRetryPolicy webExceptionRetryPolicy;

    public MetadataRequestRetryPolicy(GlobalEndpointManager globalEndpointManager) {
        this.globalEndpointManager = globalEndpointManager;
    }

    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.webExceptionRetryPolicy = new WebExceptionRetryPolicy(BridgeInternal.getRetryContext(request.requestContext.cosmosDiagnostics));
        this.webExceptionRetryPolicy.onBeforeSendRequest(request);
    }

    private boolean shouldMarkRegionAsUnavailable(CosmosException exception) {

        if (!(request.isAddressRefresh() || request.isMetadataRequest())) {
            return false;
        }

        // check for network issues or connectivity issues
        if (WebExceptionUtility.isNetworkFailure(exception)) {
            return Exceptions.isSubStatusCode(exception, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);
        }

        return false;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {

        if (webExceptionRetryPolicy == null || request == null) {
            logger.error("onBeforeSendRequest has not been invoked with the MetadataRequestRetryPolicy...");
            return Mono.just(ShouldRetryResult.error(e));
        }

        return webExceptionRetryPolicy.shouldRetry(e).flatMap(shouldRetryResult -> {

            if (!shouldRetryResult.shouldRetry) {
                if (this.request == null) {
                    logger.error("onBeforeSendRequest has not been invoked with the MetadataRequestRetryPolicy...");
                    return Mono.just(ShouldRetryResult.error(e));
                }

                // Bubble up to downstream retry policy
                if (!(e instanceof CosmosException)) {
                    logger.debug("exception is not an instance of CosmosException...");
                    return Mono.just(ShouldRetryResult.error(e));
                }

                CosmosException cosmosException = Utils.as(e, CosmosException.class);

                if (shouldMarkRegionAsUnavailable(cosmosException)) {

                    if (request.requestContext != null && request.requestContext.regionalRoutingContextToRoute != null) {

                        RegionalRoutingContext regionalRoutingContext = request.requestContext.regionalRoutingContextToRoute;
                        URI locationEndpointToRoute = regionalRoutingContext.getGatewayRegionalEndpoint();

                        if (request.isReadOnlyRequest()) {
                            logger.warn("Marking the endpoint : {} as unavailable for read.", locationEndpointToRoute);
                            this.globalEndpointManager.markEndpointUnavailableForRead(locationEndpointToRoute);
                        } else {
                            logger.warn("Marking the endpoint : {} as unavailable for write.", locationEndpointToRoute);
                            this.globalEndpointManager.markEndpointUnavailableForWrite(locationEndpointToRoute);
                        }
                    }
                }

                return Mono.just(ShouldRetryResult.error(cosmosException));
            }

            return Mono.just(shouldRetryResult);
        });
    }

    @Override
    public RetryContext getRetryContext() {
        return null;
    }
}
