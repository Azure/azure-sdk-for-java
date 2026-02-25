// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.InCompleteRoutingMapException;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class InCompleteRoutingMapRetryPolicy implements IRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(InCompleteRoutingMapRetryPolicy.class);
    private final static int MAX_RETRIES = 1;
    private int currentAttemptCount;

    public InCompleteRoutingMapRetryPolicy() {
        this.currentAttemptCount = 0;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        if (e instanceof InCompleteRoutingMapException) {
            if (this.currentAttemptCount < MAX_RETRIES) {
                this.currentAttemptCount++;

                logger.warn(
                    "Operation will be retried for InCompleteRoutingMapException. Current attempt {}",
                    this.currentAttemptCount,
                    e);
                return Mono.just(ShouldRetryResult.RETRY_NOW);
            } else {
                logger.error(
                    "Retried {} times. All retries exhausted, operation will NOT be retried for InCompleteRoutingMapException",
                    this.currentAttemptCount,
                    e);
                return Mono.just(ShouldRetryResult.NO_RETRY);
            }
        } else {
            logger.debug("Operation will NOT be retried - not incomplete routing map exception", e);
            return Mono.just(ShouldRetryResult.NO_RETRY);
        }
    }

    @Override
    public RetryContext getRetryContext() {
        return null;
    }
}
