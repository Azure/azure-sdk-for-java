// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.InCompleteRoutingMapException;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class InCompleteRoutingMapRetryPolicy implements IRetryPolicy {
    private final static Logger logger = LoggerFactory.getLogger(InCompleteRoutingMapRetryPolicy.class);
    private final static int MAX_INCOMPLETE_ROUTING_MAP_RETRIES = 1;
    private final static int MAX_PARTITION_KEY_RANGE_NOT_FOUND_RETRIES = 10;
    private final static long PARTITION_KEY_RANGE_NOT_FOUND_INITIAL_BACKOFF_IN_MILLIS = 100;
    private final static long PARTITION_KEY_RANGE_NOT_FOUND_MAX_BACKOFF_IN_MILLIS = 1000;
    private int incompleteRoutingMapAttemptCount;
    private int partitionKeyRangeNotFoundAttemptCount;

    public InCompleteRoutingMapRetryPolicy() {
        this.incompleteRoutingMapAttemptCount = 0;
        this.partitionKeyRangeNotFoundAttemptCount = 0;
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        if (e instanceof InCompleteRoutingMapException) {
            if (this.incompleteRoutingMapAttemptCount < MAX_INCOMPLETE_ROUTING_MAP_RETRIES) {
                this.incompleteRoutingMapAttemptCount++;

                logger.warn(
                    "Operation will be retried for InCompleteRoutingMapException. Current attempt {}",
                    this.incompleteRoutingMapAttemptCount,
                    e);
                return Mono.just(ShouldRetryResult.RETRY_NOW);
            } else {
                logger.error(
                    "Retried {} times. All retries exhausted, operation will NOT be retried for InCompleteRoutingMapException",
                    this.incompleteRoutingMapAttemptCount,
                    e);
                return Mono.just(ShouldRetryResult.NO_RETRY);
            }
        } else if (isRetryablePartitionKeyRangeNotFound(e)) {
            if (this.partitionKeyRangeNotFoundAttemptCount < MAX_PARTITION_KEY_RANGE_NOT_FOUND_RETRIES) {
                this.partitionKeyRangeNotFoundAttemptCount++;
                Duration retryDelay = getPartitionKeyRangeNotFoundRetryDelay(this.partitionKeyRangeNotFoundAttemptCount);

                logger.warn(
                    "Operation will be retried because partition key ranges are not available yet. Current attempt {}, retryDelay {} ms",
                    this.partitionKeyRangeNotFoundAttemptCount,
                    retryDelay.toMillis(),
                    e);
                return Mono.just(ShouldRetryResult.retryAfter(retryDelay));
            } else {
                logger.error(
                    "Retried {} times. All retries exhausted, operation will NOT be retried for partition key range NotFound",
                    this.partitionKeyRangeNotFoundAttemptCount,
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

    private static boolean isRetryablePartitionKeyRangeNotFound(Exception error) {
        CosmosException cosmosException = Utils.as(error, CosmosException.class);
        if (cosmosException == null || !Exceptions.isStatusCode(cosmosException, HttpConstants.StatusCodes.NOTFOUND)) {
            return false;
        }

        // This retry policy is only used while building the partition key range routing map.
        // 404/0 must not be treated as retryable for general item/container requests.
        int subStatusCode = cosmosException.getSubStatusCode();
        return subStatusCode == HttpConstants.SubStatusCodes.UNKNOWN
            || subStatusCode == HttpConstants.SubStatusCodes.OWNER_RESOURCE_NOT_EXISTS
            || subStatusCode == HttpConstants.SubStatusCodes.COLLECTION_NOT_AVAILABLE_FOR_READ;
    }

    private static Duration getPartitionKeyRangeNotFoundRetryDelay(int attemptCount) {
        long backoffMillis = PARTITION_KEY_RANGE_NOT_FOUND_INITIAL_BACKOFF_IN_MILLIS;
        for (int attempt = 1; attempt < attemptCount; attempt++) {
            backoffMillis = Math.min(backoffMillis * 2, PARTITION_KEY_RANGE_NOT_FOUND_MAX_BACKOFF_IN_MILLIS);
        }

        return Duration.ofMillis(backoffMillis);
    }
}
