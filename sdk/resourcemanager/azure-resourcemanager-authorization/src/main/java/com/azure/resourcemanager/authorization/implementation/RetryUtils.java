// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.implementation;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

class RetryUtils {

    // for MSGraph API
    static RetryBackoffSpec backoffRetryFor404ResourceNotFound() {
        return Retry
            // 10 + 20 + 40 = 70 seconds
            .backoff(3, ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(10)))
            .filter(throwable -> {
                boolean resourceNotFoundException = false;
                if (throwable instanceof ManagementException) {
                    ManagementException exception = (ManagementException) throwable;
                    if (exception.getResponse().getStatusCode() == 404
                        && exception.getValue() != null
                        && "Request_ResourceNotFound".equalsIgnoreCase(exception.getValue().getCode())) {
                        resourceNotFoundException = true;
                    }
                }
                return resourceNotFoundException;
            })
            // do not convert to RetryExhaustedException
            .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    // for Microsoft.Authorization API
    static RetryBackoffSpec backoffRetryFor400PrincipalNotFound() {
        return Retry
            // 10 + 20 + 40 = 70 seconds
            .backoff(3, ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(10)))
            .filter(throwable -> {
                boolean resourceNotFoundException = false;
                if (throwable instanceof ManagementException) {
                    ManagementException exception = (ManagementException) throwable;
                    if (exception.getResponse().getStatusCode() == 400
                        && exception.getValue() != null
                        && "PrincipalNotFound".equalsIgnoreCase(exception.getValue().getCode())) {
                        resourceNotFoundException = true;
                    }
                }
                return resourceNotFoundException;
            })
            // do not convert to RetryExhaustedException
            .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }
}
