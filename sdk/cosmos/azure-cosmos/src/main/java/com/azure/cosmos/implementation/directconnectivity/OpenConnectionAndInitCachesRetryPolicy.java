// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.ResourceThrottleRetryPolicy;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * This retry policy will only be used for openConnectionsAndInitCaches.
 * It should only retry when:
 * 1. If SDK get GATEWAY_ENDPOINT_READ_TIMEOUT
 * 2. If SDK get 429
 */
public class OpenConnectionAndInitCachesRetryPolicy implements IRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(OpenConnectionAndInitCachesRetryPolicy.class);
    private final static int MAX_ADDRESS_RETRY_COUNT = 2;

    private final AtomicInteger queryPlanAddressRefreshCount;
    private final ResourceThrottleRetryPolicy resourceThrottleRetryPolicy;


    public OpenConnectionAndInitCachesRetryPolicy(ThrottlingRetryOptions throttlingRetryOptions) {
        checkNotNull(throttlingRetryOptions, "Argument 'throttlingRetryOptions' should not be null");

        this.queryPlanAddressRefreshCount = new AtomicInteger(0);
        this.resourceThrottleRetryPolicy = new ResourceThrottleRetryPolicy(
                throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(),
                throttlingRetryOptions.getMaxRetryWaitTime(),
                false);
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        if (WebExceptionUtility.isNetworkFailure(e)) {
            CosmosException clientException = Utils.as(e, CosmosException.class);
            if (clientException != null &&
                    WebExceptionUtility.isReadTimeoutException(clientException) &&
                    Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT)) {

                if (this.queryPlanAddressRefreshCount.getAndIncrement() > MAX_ADDRESS_RETRY_COUNT) {
                    // TODO: logging with exception doesn't use formatting, this should be changed slightly
                    logger.warn("Received {} after exhausted all retry.", e);
                    return Mono.just(ShouldRetryResult.NO_RETRY);
                }

                return Mono.just(ShouldRetryResult.retryAfter(Duration.ZERO));
            }
        }

        return this.resourceThrottleRetryPolicy.shouldRetry(e);
    }

    @Override
    public RetryContext getRetryContext() {
        return null;
    }
}
