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
        return backoffRetry(404, "Request_ResourceNotFound");
    }

    // for MSGraph API, when appId of the service principal does not reference a valid application object
    static RetryBackoffSpec backoffRetryFor400BadRequest() {
        return backoffRetry(400, "Request_BadRequest");
    }

    // for Microsoft.Authorization API
    static RetryBackoffSpec backoffRetryFor400PrincipalNotFound() {
        return backoffRetry(400, "PrincipalNotFound");
    }

    private static RetryBackoffSpec backoffRetry(int statusCode, String errorCode) {
        return Retry
            // 10 + 20 + 40 = 70 seconds
            .backoff(3, ResourceManagerUtils.InternalRuntimeContext.getDelayDuration(Duration.ofSeconds(10)))
            .filter(throwable -> {
                boolean resourceNotFoundException = false;
                if (throwable instanceof ManagementException) {
                    ManagementException exception = (ManagementException) throwable;
                    if (exception.getResponse().getStatusCode() == statusCode
                        && exception.getValue() != null
                        && errorCode.equalsIgnoreCase(exception.getValue().getCode())) {
                        resourceNotFoundException = true;
                    }
                }
                return resourceNotFoundException;
            })
            // do not convert to RetryExhaustedException
            .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }
}
