// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import org.springframework.core.NestedRuntimeException;

/**
 * The Azure Event Hubs specific {@link NestedRuntimeException}.
 *
 */
public class EventHubsRuntimeException extends NestedRuntimeException {

    public EventHubsRuntimeException(String msg) {
        super(msg);
    }

    public EventHubsRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
