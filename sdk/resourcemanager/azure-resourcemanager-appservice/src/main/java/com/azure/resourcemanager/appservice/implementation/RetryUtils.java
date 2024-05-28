// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

class RetryUtils {

    public static <T> Mono<T> backoffRetryForFunctionAppAca(Mono<T> asyncOperation) {
        return asyncOperation.retryWhen(backoffRetry(409, 429));
    }

    private static RetryBackoffSpec backoffRetry(int... statusCodes) {
        Set<Integer> statusCodeSet = new HashSet<>();
        for (int statusCode : statusCodes) {
            statusCodeSet.add(statusCode);
        }
        return Retry
            // 10 + 20 + 40 + 80 + 160 = 310 seconds
            .backoff(5, ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(10)))
            // do not convert to RetryExhaustedException
            .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }
}
