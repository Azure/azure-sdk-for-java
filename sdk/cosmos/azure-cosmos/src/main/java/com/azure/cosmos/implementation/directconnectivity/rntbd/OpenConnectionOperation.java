// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.OpenConnectionAggressivenessHint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public final class OpenConnectionOperation {

    private final IOpenConnectionsHandler openConnectionsHandler;
    private final String collectionRid;
    private final URI serviceEndpoint;
    private final Uri addressUri;
    private int minConnectionsRequiredForEndpoint;
    private final IRetryPolicy retryPolicy;

    public OpenConnectionOperation(
            IOpenConnectionsHandler openConnectionsHandler,
            String collectionRid, URI serviceEndpoint,
            Uri addressUri,
            int minConnectionsRequiredForEndpoint
    ) {
        this.openConnectionsHandler = openConnectionsHandler;
        this.collectionRid = collectionRid;
        this.serviceEndpoint = serviceEndpoint;
        this.addressUri = addressUri;
        this.minConnectionsRequiredForEndpoint = minConnectionsRequiredForEndpoint;
        this.retryPolicy = new ProactiveOpenConnectionsRetryPolicy();
    }

    public IOpenConnectionsHandler getOpenConnectionsHandler() {
        return openConnectionsHandler;
    }

    public URI getServiceEndpoint() {
        return serviceEndpoint;
    }

    public Uri getAddressUri() {
        return addressUri;
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public int getMinConnectionsRequiredForEndpoint() {
        return minConnectionsRequiredForEndpoint;
    }

    public IRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setMinConnectionsRequiredForEndpoint(int minConnectionsRequiredForEndpoint) {
        this.minConnectionsRequiredForEndpoint = minConnectionsRequiredForEndpoint;
    }

    private static class ProactiveOpenConnectionsRetryPolicy implements IRetryPolicy {

        private static final Logger logger = LoggerFactory.getLogger(ProactiveOpenConnectionsProcessor.class);
        private static final int MaxRetryAttempts = 3;
        private static final Duration InitialOpenConnectionReattemptBackOffInMs = Duration.ofMillis(10);
        private static final Duration MaxFailedOpenConnectionRetryWindowInMs = Duration.ofMillis(10_000);
        private static final int BackoffMultiplier = 2;
        private Duration currentBackoff;
        private final TimeoutHelper waitTimeTimeoutHelper;
        private final AtomicInteger retryCount;

        private ProactiveOpenConnectionsRetryPolicy() {
            this.waitTimeTimeoutHelper = new TimeoutHelper(MaxFailedOpenConnectionRetryWindowInMs);
            this.retryCount = new AtomicInteger(0);
            this.currentBackoff = InitialOpenConnectionReattemptBackOffInMs;
        }

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception e) {

            logger.debug("In retry policy: {}, retry attempt: {}", this.getClass().getName(), this.retryCount.get());

            if (this.retryCount.get() >= MaxRetryAttempts || this.waitTimeTimeoutHelper.isElapsed() || e == null) {
                return Mono.just(ShouldRetryResult.noRetry());
            }

            this.retryCount.incrementAndGet();

            Duration effectiveBackoff = getEffectiveBackoff(this.currentBackoff, this.waitTimeTimeoutHelper.getRemainingTime());
            this.currentBackoff = getEffectiveBackoff(Duration.ofMillis(this.currentBackoff.toMillis() * BackoffMultiplier), MaxFailedOpenConnectionRetryWindowInMs);

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
