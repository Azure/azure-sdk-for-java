// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.exceptions;

import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * This exception is mainly used to indicate the exception happens during throughput control initialization.
 * This exception can be used to determine whether the request will continue to original request flow if fallBackOnInitError is true.
 *
 * This exception wraps the true underlying exception
 * if request should fail, then {@link ThroughputControlStore} will be responsible for throw the underlying exception to upcaller.
 */
public class ThroughputControlInitializationException extends RuntimeException {
    private final Throwable cause;

    public ThroughputControlInitializationException(Throwable throwable) {
        checkNotNull(throwable, "Throwable can not be null");
        this.cause = throwable;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
