// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import org.springframework.core.NestedRuntimeException;

/**
 * The Azure Event Hubs specific {@link NestedRuntimeException}.
 *
 */
public final class EventHubsRuntimeException extends NestedRuntimeException {

    /**
     * Construct {@code EventHubsRuntimeException} with the specified detail message.
     * @param msg the exception information.
     */
    public EventHubsRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Construct {@code EventHubsRuntimeException} with the specified detail message and nested exception.
     * @param msg the specified detail message.
     * @param cause the nested exception.
     */
    public EventHubsRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
