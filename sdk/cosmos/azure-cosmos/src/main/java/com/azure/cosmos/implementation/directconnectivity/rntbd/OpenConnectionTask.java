// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class OpenConnectionTask extends CompletableFuture<OpenConnectionResponse> {
    private final String collectionRid;
    private final URI serviceEndpoint;
    private final Uri addressUri;
    private int minConnectionsRequiredForEndpoint;
    private final IRetryPolicy retryPolicy;

    public OpenConnectionTask(
            String collectionRid,
            URI serviceEndpoint,
            Uri addressUri,
            int minConnectionsRequiredForEndpoint) {
        this.collectionRid = collectionRid;
        this.serviceEndpoint = serviceEndpoint;
        this.addressUri = addressUri;
        this.minConnectionsRequiredForEndpoint = minConnectionsRequiredForEndpoint;
        this.retryPolicy = new ProactiveOpenConnectionsRetryPolicy();
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public URI getServiceEndpoint() {
        return serviceEndpoint;
    }

    public Uri getAddressUri() {
        return addressUri;
    }

    public int getMinConnectionsRequiredForEndpoint() {
        return minConnectionsRequiredForEndpoint;
    }

    public void setMinConnectionsRequiredForEndpoint(int minConnectionsRequiredForEndpoint) {
        this.minConnectionsRequiredForEndpoint = minConnectionsRequiredForEndpoint;
    }

    public IRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    private static class ProactiveOpenConnectionsRetryPolicy implements IRetryPolicy {

        private static final Logger logger = LoggerFactory.getLogger(ProactiveOpenConnectionsRetryPolicy.class);
        private static final int MAX_RETRY_ATTEMPTS = 2;
        private static final Duration INITIAL_OPEN_CONNECTION_REATTEMPT_BACK_OFF_IN_MS = Duration.ofMillis(1_000);
        private static final Duration MAX_FAILED_OPEN_CONNECTION_RETRY_WINDOW_IN_MS = Duration.ofMillis(15_000);
        private static final int BACKOFF_MULTIPLIER = 4;
        private Duration currentBackoff;
        private final TimeoutHelper waitTimeTimeoutHelper;
        private final AtomicInteger retryCount;

        private ProactiveOpenConnectionsRetryPolicy() {
            this.waitTimeTimeoutHelper = new TimeoutHelper(MAX_FAILED_OPEN_CONNECTION_RETRY_WINDOW_IN_MS);
            this.retryCount = new AtomicInteger(0);
            this.currentBackoff = INITIAL_OPEN_CONNECTION_REATTEMPT_BACK_OFF_IN_MS;
        }

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception e) {

            if (this.retryCount.get() >= MAX_RETRY_ATTEMPTS || this.waitTimeTimeoutHelper.isElapsed() || e == null) {
                logger.debug("In retry policy: ProactiveOpenConnectionsRetryPolicy, retry attempt will not be performed");
                return Mono.just(ShouldRetryResult.noRetry());
            }

            logger.debug("In retry policy: ProactiveOpenConnectionsRetryPolicy, retry attempt: {}, exception :{}", this.retryCount.get(), e.getMessage());

            this.retryCount.incrementAndGet();

            Duration effectiveBackoff = getEffectiveBackoff(this.currentBackoff, this.waitTimeTimeoutHelper.getRemainingTime());
            this.currentBackoff = getEffectiveBackoff(Duration.ofMillis(this.currentBackoff.toMillis() * BACKOFF_MULTIPLIER), MAX_FAILED_OPEN_CONNECTION_RETRY_WINDOW_IN_MS);

            return Mono.just(ShouldRetryResult.retryAfter(effectiveBackoff));
        }

        @Override
        public RetryContext getRetryContext() {
            return null;
        }

        private static Duration getEffectiveBackoff(Duration backoff, Duration remainingTime) {
            if (backoff.compareTo(remainingTime) > 0) {
                return remainingTime;
            }

            return backoff;
        }
    }
}
