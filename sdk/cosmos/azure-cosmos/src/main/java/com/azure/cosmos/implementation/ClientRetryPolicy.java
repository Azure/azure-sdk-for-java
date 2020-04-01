// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosResponseDiagnostics;
import com.azure.cosmos.ThrottlingRetryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 *  Client policy is combination of endpoint change retry + throttling retry.
 */
public class ClientRetryPolicy extends DocumentClientRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(ClientRetryPolicy.class);

    final static int RetryIntervalInMS = 1000; //Once we detect failover wait for 1 second before retrying request.
    final static int MaxRetryCount = 120;

    private final DocumentClientRetryPolicy throttlingRetry;
    private final GlobalEndpointManager globalEndpointManager;
    private final boolean enableEndpointDiscovery;
    private int failoverRetryCount;

    private int sessionTokenRetryCount;
    private boolean isReadRequest;
    private boolean canUseMultipleWriteLocations;
    private URI locationEndpoint;
    private RetryContext retryContext;
    private CosmosResponseDiagnostics cosmosResponseDiagnostics;
    private AtomicInteger cnt = new AtomicInteger(0);

    public ClientRetryPolicy(GlobalEndpointManager globalEndpointManager,
                             boolean enableEndpointDiscovery,
                             ThrottlingRetryOptions throttlingRetryOptions) {

        this.throttlingRetry = new ResourceThrottleRetryPolicy(
                throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(),
                throttlingRetryOptions.getMaxRetryWaitTime());
        this.globalEndpointManager = globalEndpointManager;
        this.failoverRetryCount = 0;
        this.enableEndpointDiscovery = enableEndpointDiscovery;
        this.sessionTokenRetryCount = 0;
        this.canUseMultipleWriteLocations = false;
        this.cosmosResponseDiagnostics = BridgeInternal.createCosmosResponseDiagnostics();
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        logger.debug("retry count {}, isReadRequest {}, canUseMultipleWriteLocations {}, due to failure:",
            cnt.incrementAndGet(),
            isReadRequest,
            canUseMultipleWriteLocations,
            e);
        if (this.locationEndpoint == null) {
            // on before request is not invoked because Document Service Request creation failed.
            logger.error("locationEndpoint is null because ClientRetryPolicy::onBeforeRequest(.) is not invoked, " +
                                 "probably request creation failed due to invalid options, serialization setting, etc.");
            return Mono.just(ShouldRetryResult.error(e));
        }

        this.retryContext = null;
        // Received 403.3 on write region, initiate the endpoint re-discovery
        CosmosClientException clientException = Utils.as(e, CosmosClientException.class);
        if (clientException != null && clientException.getResponseDiagnostics() != null) {
            this.cosmosResponseDiagnostics = clientException.getResponseDiagnostics();
        }
        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.FORBIDDEN) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN))
        {
            logger.warn("Endpoint not writable. Will refresh cache and retry ", e);
            return this.shouldRetryOnEndpointFailureAsync(false, true);
        }

        // Regional endpoint is not available yet for reads (e.g. add/ online of region is in progress)
        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.FORBIDDEN) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.DATABASE_ACCOUNT_NOTFOUND) &&
                this.isReadRequest)
        {
            logger.warn("Endpoint not available for reads. Will refresh cache and retry. ", e);
            return this.shouldRetryOnEndpointFailureAsync(true, false);
        }

        // Received Connection error (HttpException), initiate the endpoint rediscovery
        if (WebExceptionUtility.isNetworkFailure(e)) {
            if (this.isReadRequest || WebExceptionUtility.isWebExceptionRetriable(e)) {
                logger.warn("Endpoint not reachable. Will refresh cache and retry. ", e);
                return this.shouldRetryOnEndpointFailureAsync(this.isReadRequest, false);
            } else {
                return this.shouldNotRetryOnEndpointFailureAsync(this.isReadRequest, false);
            }
        }

        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.NOTFOUND) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)) {
            return Mono.just(this.shouldRetryOnSessionNotAvailable());
        }

        return this.throttlingRetry.shouldRetry(e);
    }

    private ShouldRetryResult shouldRetryOnSessionNotAvailable() {
        this.sessionTokenRetryCount++;

        if (!this.enableEndpointDiscovery) {
            // if endpoint discovery is disabled, the request cannot be retried anywhere else
            return ShouldRetryResult.noRetry();
        } else {
            if (this.canUseMultipleWriteLocations) {
                UnmodifiableList<URI> endpoints = this.isReadRequest ? this.globalEndpointManager.getReadEndpoints() : this.globalEndpointManager.getWriteEndpoints();

                if (this.sessionTokenRetryCount > endpoints.size()) {
                    // When use multiple write locations is true and the request has been tried
                    // on all locations, then don't retry the request
                    return ShouldRetryResult.noRetry();
                } else {
                    this.retryContext = new RetryContext(this.sessionTokenRetryCount - 1, this.sessionTokenRetryCount > 1);
                    return ShouldRetryResult.retryAfter(Duration.ZERO);
                }
            } else {
                if (this.sessionTokenRetryCount > 1) {
                    // When cannot use multiple write locations, then don't retry the request if
                    // we have already tried this request on the write location
                    return ShouldRetryResult.noRetry();
                } else {
                    this.retryContext = new RetryContext(this.sessionTokenRetryCount - 1, false);
                    return ShouldRetryResult.retryAfter(Duration.ZERO);
                }
            }
        }
    }

    private Mono<ShouldRetryResult> shouldRetryOnEndpointFailureAsync(boolean isReadRequest , boolean forceRefresh) {
        if (!this.enableEndpointDiscovery || this.failoverRetryCount > MaxRetryCount) {
            logger.warn("ShouldRetryOnEndpointFailureAsync() Not retrying. Retry count = {}", this.failoverRetryCount);
            return Mono.just(ShouldRetryResult.noRetry());
        }

        Mono<Void> refreshLocationCompletable = this.refreshLocation(isReadRequest, forceRefresh);

        // Some requests may be in progress when the endpoint manager and client are closed.
        // In that case, the request won't succeed since the http client is closed.
        // Therefore just skip the retry here to avoid the delay because retrying won't go through in the end.

        Duration retryDelay = Duration.ZERO;
        if (!isReadRequest) {
            logger.debug("Failover happening. retryCount {}",  this.failoverRetryCount);
            if (this.failoverRetryCount > 1) {
                //if retried both endpoints, follow regular retry interval.
                retryDelay = Duration.ofMillis(ClientRetryPolicy.RetryIntervalInMS);
            }
        } else {
            retryDelay = Duration.ofMillis(ClientRetryPolicy.RetryIntervalInMS);
        }
        return refreshLocationCompletable.then(Mono.just(ShouldRetryResult.retryAfter(retryDelay)));
    }

    private Mono<ShouldRetryResult> shouldNotRetryOnEndpointFailureAsync(boolean isReadRequest , boolean forceRefresh) {
        if (!this.enableEndpointDiscovery || this.failoverRetryCount > MaxRetryCount) {
            logger.warn("ShouldRetryOnEndpointFailureAsync() Not retrying. Retry count = {}", this.failoverRetryCount);
            return Mono.just(ShouldRetryResult.noRetry());
        }
        Mono<Void> refreshLocationCompletable = this.refreshLocation(isReadRequest, forceRefresh);
        return refreshLocationCompletable.then(Mono.just(ShouldRetryResult.noRetry()));
    }

    private Mono<Void> refreshLocation(boolean isReadRequest, boolean forceRefresh) {
        this.failoverRetryCount++;

        // Mark the current read endpoint as unavailable
        if (isReadRequest) {
            logger.warn("marking the endpoint {} as unavailable for read",this.locationEndpoint);
            this.globalEndpointManager.markEndpointUnavailableForRead(this.locationEndpoint);
        } else {
            logger.warn("marking the endpoint {} as unavailable for write",this.locationEndpoint);
            this.globalEndpointManager.markEndpointUnavailableForWrite(this.locationEndpoint);
        }

        this.retryContext = new RetryContext(this.failoverRetryCount, false);
        return this.globalEndpointManager.refreshLocationAsync(null, forceRefresh);
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.isReadRequest = request.isReadOnlyRequest();
        this.canUseMultipleWriteLocations = this.globalEndpointManager.canUseMultipleWriteLocations(request);
        if (request.requestContext != null) {
            request.requestContext.cosmosResponseDiagnostics = this.cosmosResponseDiagnostics;
        }

        // clear previous location-based routing directive
        if (request.requestContext != null) {
            request.requestContext.clearRouteToLocation();
        }
        if (this.retryContext != null) {
            // set location-based routing directive based on request retry context
            request.requestContext.routeToLocation(this.retryContext.retryCount, this.retryContext.retryRequestOnPreferredLocations);
        }

        // Resolve the endpoint for the request and pin the resolution to the resolved endpoint
        // This enables marking the endpoint unavailability on endpoint failover/unreachability
        this.locationEndpoint = this.globalEndpointManager.resolveServiceEndpoint(request);
        if (request.requestContext != null) {
            request.requestContext.routeToLocation(this.locationEndpoint);
        }
    }
    private static class RetryContext {

        public int retryCount;
        public boolean retryRequestOnPreferredLocations;

        public RetryContext(int retryCount,
                            boolean retryRequestOnPreferredLocations) {
            this.retryCount = retryCount;
            this.retryRequestOnPreferredLocations = retryRequestOnPreferredLocations;
        }
    }
}
