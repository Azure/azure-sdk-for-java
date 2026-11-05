// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

final class GatewayV2SessionTokenRetryPolicy extends DocumentClientRetryPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayV2SessionTokenRetryPolicy.class);

    private RxDocumentServiceRequest request;
    private RegionalRoutingContext retryRegion;
    private String originalSessionToken;
    private boolean retried;

    boolean hasRetried() {
        return this.retried;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        if (this.retried) {
            request.useGatewayMode = true;
            request.useThinClientMode = false;
            SessionTokenHelper.setOriginalSessionToken(request, this.originalSessionToken);
            request.requestContext.sessionToken = null;
            if (this.retryRegion != null) {
                request.requestContext.routeToLocation(this.retryRegion);
                this.retryRegion = null;
            }
        }
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        if (!(exception instanceof GatewayFallbackRequiredException)) {
            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }

        if (this.retried || this.request == null || !this.request.useThinClientMode || this.request.useGatewayMode
            || !SessionTokenHelper.isCompoundSessionToken(this.request.getOriginalSessionToken())
            || this.request.requestContext == null
            || this.request.requestContext.regionalRoutingContextToRoute == null) {
            return Mono.just(ShouldRetryResult.noRetry());
        }

        this.retried = true;
        this.retryRegion = this.request.requestContext.regionalRoutingContextToRoute;
        this.originalSessionToken = this.request.getOriginalSessionToken();
        LOGGER.debug("Retrying unresolved Gateway V2 session-token scope through classic Gateway in the same region");
        return Mono.just(ShouldRetryResult.RETRY_NOW);
    }

    @Override
    public RetryContext getRetryContext() {
        return this.request == null || this.request.requestContext.cosmosDiagnostics == null
            ? null : BridgeInternal.getRetryContext(this.request.requestContext.cosmosDiagnostics);
    }

    // Raised only before dispatch, so even non-idempotent operations can safely change transport once.
    static final class GatewayFallbackRequiredException extends BadRequestException {
        GatewayFallbackRequiredException() {
            super("The compound session token requires classic Gateway to resolve the request's partition scope.");
        }
    }
}
