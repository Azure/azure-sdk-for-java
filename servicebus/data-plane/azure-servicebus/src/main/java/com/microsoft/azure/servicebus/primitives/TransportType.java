/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

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

    TransportType(final String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return this.value;
    }

    static TransportType fromString(final String value)
    {
        for (TransportType transportType : values())
        {
            if (transportType.value.equalsIgnoreCase(value))
            {
                return transportType;
            }
        }

        throw new IllegalArgumentException();
    }
}