// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.util.Locale;

/**
 * All TransportType switches available for communicating to EventHubs service.
 */
public enum TransportType {
    /**
     * AMQP over TCP. Uses port 5671 - assigned by IANA for secure AMQP (AMQPS).
     */
    AMQP("Amqp"),

    /**
     * AMQP over Web Sockets. Uses port 443.
     */
    AMQP_WEB_SOCKETS("AmqpWebSockets");

    private final String value;

    TransportType(final String value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }

    public static TransportType fromString(final String value) {
        for (TransportType transportType : values()) {
            if (transportType.value.equalsIgnoreCase(value)) {
                return transportType;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "Could not convert %s to a TransportType", value));
    }
}
