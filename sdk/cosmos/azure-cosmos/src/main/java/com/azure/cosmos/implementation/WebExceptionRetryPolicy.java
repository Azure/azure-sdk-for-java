// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicy;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicyDefault;
import com.azure.cosmos.implementation.http.ResponseTimeoutAndDelays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WebExceptionRetryPolicy implements IRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(WebExceptionRetryPolicy.class);
    // Address Refresh will be re-tried 3 times, please check the if condition carefully :)
    private final static int MAX_ADDRESS_REFRESH_RETRY_COUNT = 2;

    private StopWatch durationTimer = new StopWatch();
    private RetryContext retryContext;
    private int retryDelay;
    private RxDocumentServiceRequest request;
    private HttpTimeoutPolicy timeoutPolicy;
    private boolean isReadRequest;
    private int retryCountTimeout = 0;
    private int addressRefreshCount;
    private URI locationEndpoint;

    public WebExceptionRetryPolicy() {
        durationTimer.start();
    }

    public WebExceptionRetryPolicy(RetryContext retryContext) {
        durationTimer.start();
        this.retryContext = retryContext;
        this.timeoutPolicy = HttpTimeoutPolicyDefault.INSTANCE;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        boolean isOutOfRetries = isOutOfRetries();
        if (isOutOfRetries) {
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetry());
        }

        if (!WebExceptionUtility.isWebExceptionRetriable(e)) {
            // Have caller propagate original exception.
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }

        // Received Connection error (HttpRequestException), initiate the endpoint rediscovery
        CosmosException webException = Utils.as(e, CosmosException.class);
        if (WebExceptionUtility.isNetworkFailure(e) && this.isReadRequest &&
            (webException != null && WebExceptionUtility.isReadTimeoutException(webException) &&
                Exceptions.isSubStatusCode(webException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT))) {

            // if operationType AddressRefresh then just retry
            if (this.request.isAddressRefresh()) {
                return shouldRetryAddressRefresh();
            }

            return Mono.just(ShouldRetryResult.retryAfter(Duration.ofSeconds(retryDelay)));
        }

        logger.warn("Received retriable web exception, will retry", e);
        return Mono.just(ShouldRetryResult.retryAfter(Duration.ofSeconds(retryDelay)));
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }

    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.isReadRequest = request.isReadOnlyRequest();
        this.timeoutPolicy = HttpTimeoutPolicy.getTimeoutPolicy(request);
        // Fetching the retryCount to correctly get the retry values from the timeout policy
        if (this.retryContext != null) {
            this.retryCountTimeout = this.retryContext.getRetryCount();
        }
        // Setting the current responseTimeout and delayForNextRequest using the timeout policy being used
        if (!isOutOfRetries()) {
            ResponseTimeoutAndDelays current = timeoutPolicy.getTimeoutAndDelaysList().get(this.retryCountTimeout);
            this.request.setResponseTimeout(current.getResponseTimeout());
            this.retryDelay = current.getDelayForNextRequestInSeconds();
        }
        this.locationEndpoint = request.requestContext.locationEndpointToRoute;
    }

    private Boolean isOutOfRetries() {
        return this.retryCountTimeout >= this.timeoutPolicy.totalRetryCount();
    }

    private Mono<ShouldRetryResult> shouldRetryAddressRefresh() {
        if (this.addressRefreshCount++ > MAX_ADDRESS_REFRESH_RETRY_COUNT) {
            logger
                .warn(
                    "shouldRetryAddressRefresh() No more retrying on endpoint {}, operationType = {}, count = {}, " +
                        "isAddressRefresh = {}",
                    this.locationEndpoint, this.request.getOperationType(), this.addressRefreshCount, this.request.isAddressRefresh());
            return Mono.just(ShouldRetryResult.noRetry());
        }

        logger
            .warn("shouldRetryAddressRefresh() Retrying on endpoint {}, operationType = {}, count = {}, " +
                    "isAddressRefresh = {}, shouldForcedAddressRefresh = {}, " +
                    "shouldForceCollectionRoutingMapRefresh = {}",
                this.locationEndpoint, this.request.getOperationType(), this.addressRefreshCount,
                this.request.isAddressRefresh(),
                this.request.shouldForceAddressRefresh(),
                this.request.forceCollectionRoutingMapRefresh);

        Duration retryDelay = Duration.ZERO;
        return Mono.just(ShouldRetryResult.retryAfter(retryDelay));
    }
}
