// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

public class GatewayRetryWithRetryPolicy implements IRetryPolicy {
    private final RetryWithRetryPolicy retryWithRetryPolicy;
    private final MetadataRequestRetryPolicy metadataRequestRetryPolicy;
    private final Instant start;

    public GatewayRetryWithRetryPolicy(
        RxDocumentServiceRequest request,
        GlobalEndpointManager globalEndpointManager,
        Integer waitTimeInSeconds) {

        this.start = Instant.now();
        RetryContext retryContext = BridgeInternal.getRetryContext(request.requestContext.cosmosDiagnostics);
        this.retryWithRetryPolicy = new RetryWithRetryPolicy(
            waitTimeInSeconds,
            retryContext,
            () -> Duration.between(this.start, Instant.now()).toMillis(),
            null);
        this.metadataRequestRetryPolicy = new MetadataRequestRetryPolicy(globalEndpointManager);
        this.metadataRequestRetryPolicy.onBeforeSendRequest(request);
    }

    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception exception) {
        return this.retryWithRetryPolicy.shouldRetry(exception).flatMap(retryWithResult -> {
            if (!retryWithResult.nonRelatedException) {
                return Mono.just(retryWithResult);
            }

            return this.metadataRequestRetryPolicy.shouldRetry(exception).map(metadataRequestRetryResult -> {
                if (metadataRequestRetryResult.shouldRetry || metadataRequestRetryResult.nonRelatedException) {
                    return metadataRequestRetryResult;
                }

                if (metadataRequestRetryResult.exception != null) {
                    return ShouldRetryResult.errorOnNonRelatedException(metadataRequestRetryResult.exception);
                }

                return ShouldRetryResult.noRetryOnNonRelatedException();
            });
        });
    }

    @Override
    public RetryContext getRetryContext() {
        return this.retryWithRetryPolicy.getRetryContext();
    }
}
