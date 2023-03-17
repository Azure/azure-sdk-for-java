// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicy;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicyControlPlaneHotPath;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicyDefault;
import com.azure.cosmos.implementation.http.ResponseTimeoutAndDelays;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebExceptionRetryPolicy extends DocumentClientRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(WebExceptionRetryPolicy.class);

    // total wait time in seconds to retry. should be max of primary reconfigrations/replication wait duration etc
    private final static int waitTimeInSeconds = 30;
//    private final static int initialBackoffSeconds = 1;
//    private final static int backoffMultiplier = 2;

    private StopWatch durationTimer = new StopWatch();
//    private int attemptCount = 0;
    // Don't penalise first retry with delay.
//    private int currentBackoffSeconds = WebExceptionRetryPolicy.initialBackoffSeconds;
    private int backoffSecondsTimeout;
    private RetryContext retryContext;
    private RxDocumentServiceRequest request;
    private HttpTimeoutPolicy timeoutPolicy;
    private HttpMethod httpMethod;
    private int retryCountTimeout = 0;

    public WebExceptionRetryPolicy() {
        durationTimer.start();
    }

    public WebExceptionRetryPolicy(RetryContext retryContext) {
        durationTimer.start();
        this.retryContext = retryContext;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
//        Duration backoffTime = Duration.ofSeconds(0);

        boolean isOutOfRetries = isOutOfRetries();
        if (isOutOfRetries || !timeoutPolicy.isSafeToRetry(httpMethod)) {
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetry());
        }

        if (isOutOfRetries || (!timeoutPolicy.isSafeToRetry(httpMethod) && !WebExceptionUtility.isWebExceptionRetriable(exception))) {
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetry());
        }

        if (!WebExceptionUtility.isWebExceptionRetriable(exception)) {
            // Have caller propagate original exception.
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }

        // Don't penalise first retry with delay.
//        if (attemptCount++ > 1) {
//            int remainingSeconds = WebExceptionRetryPolicy.waitTimeInSeconds - Math.toIntExact(this.durationTimer.getTime(TimeUnit.SECONDS));
//            if (remainingSeconds <= 0) {
//                this.durationTimer.stop();
//                return Mono.just(ShouldRetryResult.noRetry());
//            }
//
//            backoffTime = Duration.ofSeconds(Math.min(this.currentBackoffSeconds, remainingSeconds));
//            this.currentBackoffSeconds *= WebExceptionRetryPolicy.backoffMultiplier;
//        }

        logger.warn("Received retriable web exception, will retry", exception);

        return Mono.just(ShouldRetryResult.retryAfter(Duration.ofSeconds(backoffSecondsTimeout)));
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        // TODO: Add implementation
        this.request = request;
        if (request.isReadOnlyRequest()) {
            this.httpMethod = HttpMethod.GET;
        }
        this.timeoutPolicy = HttpTimeoutPolicyDefault.instance;
        if (OperationType.QueryPlan.equals(request.getOperationType()) || request.isAddressRefresh()) {
            timeoutPolicy = HttpTimeoutPolicyControlPlaneHotPath.instance;
        }

        // Fetching the retryCount to correctly get the retry values from the timeout policy
        if (this.retryContext != null) {
            this.retryCountTimeout = this.retryContext.getRetryCount();
        }

        // Setting the current responseTimeout and delayForNextRequest using the timeout policy being used
        ResponseTimeoutAndDelays current = timeoutPolicy.getTimeoutList().get(this.retryCountTimeout);
        this.request.setResponseTimeout(current.getResponseTimeout());
        this.backoffSecondsTimeout = current.getDelayForNextRequest();
    }

    private Boolean isOutOfRetries() {
        return (WebExceptionRetryPolicy.waitTimeInSeconds -
            Math.toIntExact(this.durationTimer.getTime(TimeUnit.SECONDS)) <= 0) || this.retryCountTimeout >= 3;
    }
}
