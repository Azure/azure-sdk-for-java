// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import java.util.Objects;

public class ServiceBusErrorContext {
    private final Throwable throwable;

    /**
     * Creates a new instance of ErServiceBusErrorContextrorContext.
     *
     * @param throwable The {@link Throwable error} that occurred.
     * @throws NullPointerException if {@code throwable} is {@code null}.
     */
    public ServiceBusErrorContext(final Throwable throwable) {
        this.throwable = Objects.requireNonNull(throwable, "'throwable' cannot be null");
    }
}
