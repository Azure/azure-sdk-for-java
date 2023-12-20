// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicy;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicyDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

public class WebExceptionRetryPolicy implements IRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(WebExceptionRetryPolicy.class);

    private StopWatch durationTimer = new StopWatch();
    private RetryContext retryContext;
    private RxDocumentServiceRequest request;
    private HttpTimeoutPolicy timeoutPolicy;
    private boolean isReadRequest;
    private int retryCount = 0;
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
        if (this.isOutOfRetries()) {
            logger
                .warn(
                    "WebExceptionRetryPolicy() No more retrying on endpoint {}, operationType = {}, count = {}, " +
                        "isAddressRefresh = {}",
                    this.locationEndpoint,
                    this.request.getOperationType(),
                    this.retryCount,
                    this.request.isAddressRefresh());

            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetry());
        }


        if (WebExceptionUtility.isNetworkFailure(e)) {
            if (this.isReadRequest
                || request.isAddressRefresh()
                || WebExceptionUtility.isWebExceptionRetriable(e)) {
                int delayInSeconds = this.timeoutPolicy.getTimeoutAndDelaysList().get(this.retryCount).getDelayForNextRequestInSeconds();
                // Increase the retry count after calculating the delay
                retryCount++;
                logger
                    .debug("WebExceptionRetryPolicy() Retrying on endpoint {}, operationType = {}, resourceType = {}, count = {}, " +
                            "isAddressRefresh = {}, shouldForcedAddressRefresh = {}, " +
                            "shouldForceCollectionRoutingMapRefresh = {}",
                        this.locationEndpoint, this.request.getOperationType(), this.request.getResourceType(), this.retryCount,
                        this.request.isAddressRefresh(),
                        this.request.shouldForceAddressRefresh(),
                        this.request.forceCollectionRoutingMapRefresh);

                this.request.setResponseTimeout(this.timeoutPolicy.getTimeoutAndDelaysList().get(this.retryCount).getResponseTimeout());
                return Mono.just(ShouldRetryResult.retryAfter(Duration.ofSeconds(delayInSeconds)));
            }
        }

        logger
            .debug(
                "WebExceptionRetryPolicy() No retrying on un-retryable exceptions on endpoint {}, operationType = {}, resourceType = {}, count = {}, " +
                    "isAddressRefresh = {}",
                this.locationEndpoint,
                this.request.getOperationType(),
                this.request.getResourceType(),
                this.retryCount,
                this.request.isAddressRefresh());


        this.durationTimer.stop();
        return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }

    // This method should only be called once
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.isReadRequest = request.isReadOnlyRequest();
        this.timeoutPolicy = HttpTimeoutPolicy.getTimeoutPolicy(request);

        // set the initial response timeout
        this.request.setResponseTimeout(timeoutPolicy.getTimeoutAndDelaysList().get(0).getResponseTimeout());
        this.locationEndpoint = request.requestContext.locationEndpointToRoute;
    }

    private boolean isOutOfRetries() {
        return this.retryCount >= this.timeoutPolicy.totalRetryCount();
    }
}
