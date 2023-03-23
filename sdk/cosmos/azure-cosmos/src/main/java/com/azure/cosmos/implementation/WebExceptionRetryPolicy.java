// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.time.StopWatch;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicy;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicyDefault;
import com.azure.cosmos.implementation.http.ResponseTimeoutAndDelays;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;;

public class WebExceptionRetryPolicy extends DocumentClientRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(WebExceptionRetryPolicy.class);

    private StopWatch durationTimer = new StopWatch();
    private int backoffSecondsTimeout;
    private RetryContext retryContext;
    private RxDocumentServiceRequest request;
    private HttpTimeoutPolicy timeoutPolicy;
    private HttpMethod httpMethod;
    private int retryCountTimeout = 0;

    public WebExceptionRetryPolicy() {
        this(null);
    }

    public WebExceptionRetryPolicy(RetryContext retryContext) {
        durationTimer.start();
        this.retryContext = retryContext;
        this.timeoutPolicy = HttpTimeoutPolicyDefault.instance;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        boolean isOutOfRetries = isOutOfRetries();
        if (isOutOfRetries) {
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetry());
        }
        if (!timeoutPolicy.isSafeToRetry(httpMethod) && !WebExceptionUtility.isWebExceptionRetriable(exception)) {
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }
        if (!WebExceptionUtility.isWebExceptionRetriable(exception)) {
            // Have caller propagate original exception.
            this.durationTimer.stop();
            return Mono.just(ShouldRetryResult.noRetryOnNonRelatedException());
        }
        logger.warn("Received retriable web exception, will retry", exception);

        return Mono.just(ShouldRetryResult.retryAfter(Duration.ofSeconds(backoffSecondsTimeout)));
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryContext;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        if (request.isReadOnlyRequest()) {
            this.httpMethod = HttpMethod.GET;
        }
        this.timeoutPolicy = HttpTimeoutPolicy.getTimeoutPolicy(request);
        // Fetching the retryCount to correctly get the retry values from the timeout policy
        if (this.retryContext != null) {
            this.retryCountTimeout = this.retryContext.getRetryCount();
        }
        // Setting the current responseTimeout and delayForNextRequest using the timeout policy being used
        if (!isOutOfRetries()) {
            ResponseTimeoutAndDelays current = timeoutPolicy.getTimeoutList().get(this.retryCountTimeout);
            this.request.setResponseTimeout(current.getResponseTimeout());
            this.backoffSecondsTimeout = current.getDelayForNextRequest();
        }
    }

    private Boolean isOutOfRetries() {
        return this.retryCountTimeout >= this.timeoutPolicy.totalRetryCount();
    }
}
