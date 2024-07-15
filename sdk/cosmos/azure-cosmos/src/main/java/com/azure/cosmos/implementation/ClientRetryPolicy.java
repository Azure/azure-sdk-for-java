// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.circuitBreaker.GlobalPartitionEndpointManagerForCircuitBreaker;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.faultinjection.FaultInjectionRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders.INTENDED_COLLECTION_RID_HEADER;

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
    private final static int MaxServiceUnavailableRetryCount = 1;

    private final DocumentClientRetryPolicy throttlingRetry;
    private final GlobalEndpointManager globalEndpointManager;
    private final boolean enableEndpointDiscovery;
    private int failoverRetryCount;

    private int sessionTokenRetryCount;
    private int staleContainerRetryCount;
    private boolean isReadRequest;
    private boolean canUseMultipleWriteLocations;
    private URI locationEndpoint;
    private RetryContext retryContext;
    private CosmosDiagnostics cosmosDiagnostics;
    private AtomicInteger cnt = new AtomicInteger(0);
    private int serviceUnavailableRetryCount;
    private RxDocumentServiceRequest request;
    private RxCollectionCache rxCollectionCache;
    private final FaultInjectionRequestContext faultInjectionRequestContext;
    private final GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker;

    public ClientRetryPolicy(DiagnosticsClientContext diagnosticsClientContext,
                             GlobalEndpointManager globalEndpointManager,
                             boolean enableEndpointDiscovery,
                             ThrottlingRetryOptions throttlingRetryOptions,
                             RxCollectionCache rxCollectionCache,
                             GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker) {

        this.globalEndpointManager = globalEndpointManager;
        this.failoverRetryCount = 0;
        this.enableEndpointDiscovery = enableEndpointDiscovery;
        this.sessionTokenRetryCount = 0;
        this.staleContainerRetryCount = 0;
        this.canUseMultipleWriteLocations = false;
        this.cosmosDiagnostics = diagnosticsClientContext.createDiagnostics();
        this.throttlingRetry = new ResourceThrottleRetryPolicy(
            throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(),
            throttlingRetryOptions.getMaxRetryWaitTime(),
            BridgeInternal.getRetryContext(this.getCosmosDiagnostics()),
            false);
        this.rxCollectionCache = rxCollectionCache;
        this.faultInjectionRequestContext = new FaultInjectionRequestContext();
        this.globalPartitionEndpointManagerForCircuitBreaker = globalPartitionEndpointManagerForCircuitBreaker;
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
        CosmosException clientException = Utils.as(e, CosmosException.class);
        if (clientException != null && clientException.getDiagnostics() != null) {
            this.cosmosDiagnostics = clientException.getDiagnostics();
        }
        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.FORBIDDEN) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.FORBIDDEN_WRITEFORBIDDEN))
        {
            logger.info("Endpoint not writable. Will refresh cache and retry ", e);
            return this.shouldRetryOnEndpointFailureAsync(false, true, false);
        }

        // Regional endpoint is not available yet for reads (e.g. add/ online of region is in progress)
        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.FORBIDDEN) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.DATABASE_ACCOUNT_NOTFOUND) &&
                this.isReadRequest)
        {
            logger.info("Endpoint not available for reads. Will refresh cache and retry. ", e);
            return this.shouldRetryOnEndpointFailureAsync(true, false, false);
        }

        // Received Connection error (HttpRequestException), initiate the endpoint rediscovery
        if (WebExceptionUtility.isNetworkFailure(e)) {
            if (clientException != null && Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE)) {
                if (this.isReadRequest || WebExceptionUtility.isWebExceptionRetriable(e)) {
                    logger.info("Gateway endpoint not reachable. Will refresh cache and retry. ", e);
                    return this.shouldRetryOnEndpointFailureAsync(this.isReadRequest, false, true);
                } else {
                    return this.shouldNotRetryOnEndpointFailureAsync(this.isReadRequest, false, false);
                }
            } else if (clientException != null &&
                WebExceptionUtility.isReadTimeoutException(clientException) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT)) {

                return shouldRetryOnGatewayTimeout();
            }
        }

        if (clientException != null &&
                Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.NOTFOUND) &&
                Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)) {
            return Mono.just(this.shouldRetryOnSessionNotAvailable(this.request));
        }

        // This is for gateway mode, collection recreate scenario is not handled there
        if (clientException != null &&
            Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.BADREQUEST) &&
            Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.INCORRECT_CONTAINER_RID_SUB_STATUS)) {
            return this.shouldRetryOnStaleContainer();
        }

        if (clientException != null &&
            Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.SERVICE_UNAVAILABLE)) {

            boolean isWebExceptionRetriable = WebExceptionUtility.isWebExceptionRetriable(e);
            logger.info(
                "Service unavailable - IsReadRequest {}, IsWebExceptionRetriable {}, NonIdempotentWriteRetriesEnabled {}",
                this.isReadRequest,
                isWebExceptionRetriable,
                this.request.getNonIdempotentWriteRetriesEnabled(),
                e);

            return this.shouldRetryOnBackendServiceUnavailableAsync(
                this.isReadRequest,
                isWebExceptionRetriable,
                this.request.getNonIdempotentWriteRetriesEnabled(),
                clientException);
        }

        if (clientException != null
            && Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.REQUEST_TIMEOUT)
            && Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.TRANSIT_TIMEOUT)) {

            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Request timeout  - IsReadRequest {}, IsWebExceptionRetriable {}, NonIdempotentWriteRetriesEnabled {}",
                    this.isReadRequest,
                    false,
                    this.request.getNonIdempotentWriteRetriesEnabled(),
                    e);
            }

            return this.shouldRetryOnRequestTimeout(
                this.isReadRequest,
                this.request.getNonIdempotentWriteRetriesEnabled()
            );
        }

        if (clientException != null && Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR)) {

            if (logger.isDebugEnabled()) {
                logger.info("Internal server error - IsReadRequest {}", this.isReadRequest, e);
            }

            return this.shouldRetryOnInternalServerError();
        }

        return this.throttlingRetry.shouldRetry(e);
    }

      private boolean canGatewayRequestFailoverOnTimeout(RxDocumentServiceRequest request) {
        //Query Plan requests
        if(request.getOperationType() == OperationType.QueryPlan) {
            return true;
        }

        //Meta data request check
        boolean isMetaDataRequest = request.isMetadataRequest();

        //Meta Data Read
        if(isMetaDataRequest && request.isReadOnly()) {
              return true;
        }

        //Data Plane Read
        if(!isMetaDataRequest
            && !request.isAddressRefresh()
            && request.isReadOnly()) {
            return true;
        }

        return false;
    }

    private ShouldRetryResult shouldRetryOnSessionNotAvailable(RxDocumentServiceRequest request) {
        this.sessionTokenRetryCount++;

        if (!this.enableEndpointDiscovery) {
            // if endpoint discovery is disabled, the request cannot be retried anywhere else
            return ShouldRetryResult.noRetry();
        } else {
            if (this.canUseMultipleWriteLocations) {
                UnmodifiableList<URI> endpoints =
                    this.isReadRequest ?
                        this.globalEndpointManager.getApplicableReadEndpoints(request) : this.globalEndpointManager.getApplicableWriteEndpoints(request);

                if (this.sessionTokenRetryCount >= endpoints.size()) {
                    // When use multiple write locations is true and the request has been tried
                    // on all locations, then don't retry the request
                    return ShouldRetryResult.noRetry();
                } else {
                    this.retryContext = new RetryContext(this.sessionTokenRetryCount , true);
                    return ShouldRetryResult.retryAfter(Duration.ZERO);
                }
            } else {
                if (this.sessionTokenRetryCount > 1) {
                    // When cannot use multiple write locations, then don't retry the request if
                    // we have already tried this request on the write location
                    return ShouldRetryResult.noRetry();
                } else {
                    this.retryContext = new RetryContext(0, false);
                    return ShouldRetryResult.retryAfter(Duration.ZERO);
                }
            }
        }
    }

    private Mono<ShouldRetryResult> shouldRetryOnStaleContainer() {
        this.staleContainerRetryCount++;
        if (this.rxCollectionCache == null || this.staleContainerRetryCount > 1) {
            return Mono.just(ShouldRetryResult.noRetry());
        }

        this.request.setForceNameCacheRefresh(true);

        // Refresh the sdk collection cache and throw the exception if intendedCollectionRid was passed by outside sdk, so caller will refresh their own collection cache if they have one
        // Cosmos encryption is one use case
        if(request.intendedCollectionRidPassedIntoSDK) {
            return this.rxCollectionCache.refreshAsync(null, this.request).then( Mono.just(ShouldRetryResult.noRetry()));
        }

        //remove the previous header and try again
        if(StringUtils.isNotEmpty(request.getHeaders().get(INTENDED_COLLECTION_RID_HEADER))) {
            request.getHeaders().remove(INTENDED_COLLECTION_RID_HEADER);
        }
        return this.rxCollectionCache.refreshAsync(null, this.request).then(Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO)));
    }

    private Mono<ShouldRetryResult> shouldRetryOnEndpointFailureAsync(boolean isReadRequest, boolean forceRefresh, boolean usePreferredLocations) {
        if (!this.enableEndpointDiscovery || this.failoverRetryCount > MaxRetryCount) {
            logger.warn("ShouldRetryOnEndpointFailureAsync() Not retrying. Retry count = {}", this.failoverRetryCount);
            return Mono.just(ShouldRetryResult.noRetry());
        }

        Mono<Void> refreshLocationCompletable = this.refreshLocation(isReadRequest, forceRefresh, usePreferredLocations);

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

    private Mono<ShouldRetryResult> shouldRetryOnGatewayTimeout() {

        boolean canFailoverOnTimeout = canGatewayRequestFailoverOnTimeout(this.request);

        if (this.globalPartitionEndpointManagerForCircuitBreaker.isPartitionLevelCircuitBreakingApplicable(this.request)) {
            this.globalPartitionEndpointManagerForCircuitBreaker.handleLocationExceptionForPartitionKeyRange(this.request, this.request.requestContext.locationEndpointToRoute);
        }

        //if operation is data plane read, metadata read, or query plan it can be retried on a different endpoint.
        if (canFailoverOnTimeout) {
            if (!this.enableEndpointDiscovery || this.failoverRetryCount > MaxRetryCount) {
                logger.warn("shouldRetryOnHttpTimeout() Not retrying. Retry count = {}", this.failoverRetryCount);
                return Mono.just(ShouldRetryResult.noRetry());
            }

            this.failoverRetryCount++;
            this.retryContext = new RetryContext(this.failoverRetryCount, true);
            Duration retryDelay = Duration.ofMillis(ClientRetryPolicy.RetryIntervalInMS);
            return Mono.just(ShouldRetryResult.retryAfter(retryDelay));
        }

        return Mono.just(ShouldRetryResult.NO_RETRY);
    }

    private Mono<ShouldRetryResult> shouldNotRetryOnEndpointFailureAsync(boolean isReadRequest , boolean forceRefresh, boolean usePreferredLocations) {
        if (!this.enableEndpointDiscovery || this.failoverRetryCount > MaxRetryCount) {
            logger.warn("ShouldRetryOnEndpointFailureAsync() Not retrying. Retry count = {}", this.failoverRetryCount);
            return Mono.just(ShouldRetryResult.noRetry());
        }
        Mono<Void> refreshLocationCompletable = this.refreshLocation(isReadRequest, forceRefresh, usePreferredLocations);
        return refreshLocationCompletable.then(Mono.just(ShouldRetryResult.noRetry()));
    }

    private Mono<Void> refreshLocation(boolean isReadRequest, boolean forceRefresh, boolean usePreferredLocations) {
        this.failoverRetryCount++;

        // Mark the current read endpoint as unavailable
        if (isReadRequest) {
            logger.warn("marking the endpoint {} as unavailable for read",this.locationEndpoint);
            this.globalEndpointManager.markEndpointUnavailableForRead(this.locationEndpoint);
        } else {
            logger.warn("marking the endpoint {} as unavailable for write",this.locationEndpoint);
            this.globalEndpointManager.markEndpointUnavailableForWrite(this.locationEndpoint);
        }

        this.retryContext = new RetryContext(this.failoverRetryCount, usePreferredLocations);
        return this.globalEndpointManager.refreshLocationAsync(null, forceRefresh);
    }

    private Mono<ShouldRetryResult> shouldRetryOnBackendServiceUnavailableAsync(
        boolean isReadRequest,
        boolean isWebExceptionRetriable,
        boolean nonIdempotentWriteRetriesEnabled,
        CosmosException cosmosException) {

        if (this.globalPartitionEndpointManagerForCircuitBreaker.isPartitionLevelCircuitBreakingApplicable(this.request)) {
            this.globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(this.request, this.request.requestContext.locationEndpointToRoute);
        }

        // The request has failed with 503, SDK need to decide whether it is safe to retry for write operations
        // For server generated retries, it is safe to retry
        // For SDK generated 503, it will be more tricky as we have to decide the cause of it. For any causes that SDK not sure whether the request
        // has reached/processed from server side, unless customer has specifically opted in for nonIdempotentWriteRetries, SDK should not retry.
        // When SDK would generate 503:
        //    - When server return 410, SDK may internally retry multiple times, when all the retries exhausted, SDK will bubble up 503 with corresponding subStatusCode
        //      (Note: currently, subStatus code for read may get lost during the conversion, but for writes, the subStatus code will be reserved)
        //    - when SDK generated 410 due to different reason (like connectionTimeout, transient timeout etc), SDK will internally retry multiple times
        //      when all the retries exhausted, SDK will bubble up 503
        //
        // Fow now, without nonIdempotentWriteRetries being enabled, SDK will only retry for the following situation:
        // 1. For any connection related errors, it will be covered under isWebExceptionRetriable -> which SDK will retry
        // 2. For any server returned 503s, SDK will retry
        // 3. For SDK generated 503, SDK will only retry if the subStatusCode is SERVER_GENERATED_410
        if (!isReadRequest
            && !shouldRetryWriteOnServiceUnavailable(
                nonIdempotentWriteRetriesEnabled,
                isWebExceptionRetriable,
                cosmosException)) {
            logger.warn(
                "shouldRetryOnBackendServiceUnavailableAsync() Not retrying" +
                    " on write with non retriable exception and non server returned service unavailable. Retry count = {}",
                this.serviceUnavailableRetryCount);
            return Mono.just(ShouldRetryResult.noRetry());
        }

        this.serviceUnavailableRetryCount++;

        if (this.serviceUnavailableRetryCount > MaxServiceUnavailableRetryCount) {
            logger.warn("shouldRetryOnBackendServiceUnavailableAsync() Not retrying. Retry count = {}", this.serviceUnavailableRetryCount);
            return Mono.just(ShouldRetryResult.noRetry());
        }

        if (!this.canUseMultipleWriteLocations && !isReadRequest) {
            // Write requests on single master cannot be retried, no other regions available
            return Mono.just(ShouldRetryResult.noRetry());
        }

        int availablePreferredLocations = this.globalEndpointManager.getPreferredLocationCount();
        if (availablePreferredLocations <= 1) {
            logger.warn("shouldRetryOnServiceUnavailable() Not retrying. No other regions available for the request. AvailablePreferredLocations = {}", availablePreferredLocations);
            return Mono.just(ShouldRetryResult.noRetry());
        }

        logger.info("shouldRetryOnServiceUnavailable() Retrying. Received on endpoint {}, IsReadRequest = {}", this.locationEndpoint, isReadRequest);

        // Retrying on second PreferredLocations
        // RetryCount is used as zero-based index
        this.retryContext = new RetryContext(this.serviceUnavailableRetryCount, true);
        return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
    }

    private Mono<ShouldRetryResult> shouldRetryOnRequestTimeout(
        boolean isReadRequest,
        boolean nonIdempotentWriteRetriesEnabled) {

        if (this.globalPartitionEndpointManagerForCircuitBreaker.isPartitionLevelCircuitBreakingApplicable(this.request)) {
            if (!isReadRequest && !nonIdempotentWriteRetriesEnabled) {
                this.globalPartitionEndpointManagerForCircuitBreaker.handleLocationExceptionForPartitionKeyRange(
                    request,
                    request.requestContext.locationEndpointToRoute);
            }
        }

        return Mono.just(ShouldRetryResult.NO_RETRY);
    }

    private Mono<ShouldRetryResult> shouldRetryOnInternalServerError() {

        if (this.globalPartitionEndpointManagerForCircuitBreaker.isPartitionLevelCircuitBreakingApplicable(this.request)) {
            this.globalPartitionEndpointManagerForCircuitBreaker.handleLocationExceptionForPartitionKeyRange(
                request,
                request.requestContext.locationEndpointToRoute);
        }

        return Mono.just(ShouldRetryResult.NO_RETRY);
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.isReadRequest = request.isReadOnlyRequest();
        this.canUseMultipleWriteLocations = this.globalEndpointManager.canUseMultipleWriteLocations(request);
        if (request.requestContext != null) {
            request.requestContext.cosmosDiagnostics = this.cosmosDiagnostics;
        }

        // clear previous location-based routing directive
        if (request.requestContext != null) {
            request.requestContext.clearRouteToLocation();
        }
        if (this.retryContext != null) {
            // set location-based routing directive based on request retry context
            request.requestContext.routeToLocation(this.retryContext.retryCount, this.retryContext.retryRequestOnPreferredLocations);
        }

        // Important: this is to make the fault injection context will not be lost between each retries
        this.request.faultInjectionRequestContext = this.faultInjectionRequestContext;

        // Resolve the endpoint for the request and pin the resolution to the resolved endpoint
        // This enables marking the endpoint unavailability on endpoint failover/unreachability
        this.locationEndpoint = this.globalEndpointManager.resolveServiceEndpoint(request);
        if (request.requestContext != null) {
            request.requestContext.routeToLocation(this.locationEndpoint);
        }
    }

    @Override
    public com.azure.cosmos.implementation.RetryContext getRetryContext() {
        return BridgeInternal.getRetryContext(this.getCosmosDiagnostics());
    }

    public boolean canUsePreferredLocations() {
        return this.retryContext != null && this.retryContext.retryRequestOnPreferredLocations;
    }

    CosmosDiagnostics getCosmosDiagnostics() {
        return cosmosDiagnostics;
    }

    private boolean shouldRetryWriteOnServiceUnavailable(
        boolean nonIdempotentWriteRetriesEnabled,
        boolean isWebExceptionRetriable,
        CosmosException cosmosException) {

        if (nonIdempotentWriteRetriesEnabled || isWebExceptionRetriable) {
            return true;
        }

        if (cosmosException instanceof ServiceUnavailableException) {
            ServiceUnavailableException serviceUnavailableException = (ServiceUnavailableException) cosmosException;
            return serviceUnavailableException.getSubStatusCode() == HttpConstants.SubStatusCodes.SERVER_GENERATED_503
                || serviceUnavailableException.getSubStatusCode() == HttpConstants.SubStatusCodes.SERVER_GENERATED_410;
        }

        return false;
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
