// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

/**
 * Represents the message state of the {@link ServiceBusReceivedMessage}.
 */
public enum ServiceBusMessageState {
    /**
     * Specifies an active message state.
     */
    ACTIVE(0),
    /**
     * Specifies a deferred message state.
     */
    DEFERRED(1),
    /**
     * Specifies a scheduled message state.
     */
    SCHEDULED(2);

    private final int value;

    ServiceBusMessageState(int value) {
        this.value = value;
    }

    /**
     * Gets the value of the message state.
     *
     * @return The value of the message state.
     */
    public int getValue() {
        return value;
    }
}
