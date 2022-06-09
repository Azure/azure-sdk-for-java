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

    /**
     * Gets the message state from {@code value}.
     *
     * @param value Integer value of the message state.
     *
     * @return The corresponding message state.
     *
     * @throws UnsupportedOperationException if {@code value} is not a known message state.
     */
    public static ServiceBusMessageState fromValue(int value) {
        switch (value) {
            case 0:
                return ServiceBusMessageState.ACTIVE;
            case 1:
                return ServiceBusMessageState.DEFERRED;
            case 2:
                return ServiceBusMessageState.SCHEDULED;
            default:
                throw new UnsupportedOperationException(
                    "Value is not supported. Should be 0(ACTIVE), 1(DEFERRED), or 2(SCHEDULED). Actual: " + value);
        }
    }
}
