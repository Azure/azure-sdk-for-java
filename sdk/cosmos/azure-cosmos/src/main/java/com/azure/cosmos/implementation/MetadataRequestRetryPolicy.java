// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
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
        this.webExceptionRetryPolicy = new WebExceptionRetryPolicy();
    }

    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
    }

    private static boolean shouldMarkRegionAsUnavailable(CosmosException exception) {

        // check for network issues or connectivity issues
        if (WebExceptionUtility.isNetworkFailure(exception)) {
            return Exceptions.isSubStatusCode(exception, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);
        }

        return false;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {

        return webExceptionRetryPolicy.shouldRetry(e).flatMap(shouldRetryResult -> {

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
                URI locationEndpointToRoute = request.requestContext.locationEndpointToRoute;

                if (request.isReadOnlyRequest()) {
                    this.globalEndpointManager.markEndpointUnavailableForRead(locationEndpointToRoute);
                } else {
                    this.globalEndpointManager.markEndpointUnavailableForWrite(locationEndpointToRoute);
                }
            }

            return Mono.just(ShouldRetryResult.error(cosmosException));
        });
    }

    @Override
    public RetryContext getRetryContext() {
        return null;
    }
}
