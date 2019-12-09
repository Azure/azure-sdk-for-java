// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import java.util.Locale;

/**
 * All TransportType switches available for AMQP protocol.
 */
public enum AmqpTransportType {
    /**
     * AMQP over TCP. Uses port 5671 - assigned by IANA for secure AMQP (AMQPS).
     */
    AMQP("Amqp"),

    /**
     * AMQP over Web Sockets. Uses port 443.
     */
    AMQP_WEB_SOCKETS("AmqpWebSockets");

    private final String value;

    AmqpTransportType(final String value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Creates an TransportType from its display value.
     *
     * @param value The string value of the TransportType.
     * @return The TransportType represented by the value.
     * @throws IllegalArgumentException If a TransportType cannot be parsed from the string value.
     */
    public static AmqpTransportType fromString(final String value) {
        for (AmqpTransportType transportType : values()) {
            if (transportType.value.equalsIgnoreCase(value)) {
                return transportType;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "Could not convert %s to a TransportType", value));
    }
}
