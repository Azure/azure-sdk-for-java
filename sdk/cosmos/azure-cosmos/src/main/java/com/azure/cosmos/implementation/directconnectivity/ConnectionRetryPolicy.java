// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import reactor.core.publisher.Mono;

public class ConnectionRetryPolicy implements IRetryPolicy {
    @Override
    public Mono<ShouldRetryResult> shouldRetry(Exception e) {
        return null;
    }

    @Override
    public RetryContext getRetryContext() {
        return null;
    }
}
