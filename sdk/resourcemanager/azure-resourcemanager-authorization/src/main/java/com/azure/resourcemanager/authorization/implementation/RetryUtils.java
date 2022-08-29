// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

class RetryUtils {

    static RetryBackoffSpec backoffRetryFor404() {
        return Retry
            // 10 + 20 + 40 = 70 seconds
            .backoff(3, ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(10)))
            .filter(e -> (e instanceof HttpResponseException)
                && (((HttpResponseException) e).getResponse().getStatusCode() == 404))
            // do not convert to RetryExhaustedException
            .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }
}
