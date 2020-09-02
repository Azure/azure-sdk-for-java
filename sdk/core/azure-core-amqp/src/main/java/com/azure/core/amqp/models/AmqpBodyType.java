// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.util.Locale;

/**
 * All AmqpBodyType available for AMQP Message.
 */
public enum AmqpBodyType {
    /**
     * Represent Amqp Data type
     */
    DATA("Data"),
    /**
     * Represent Amqp Value type
     */
    VALUE("Value"),
    /**
     * Represent Amqp Sequence type
     */
    SEQUENCE("Sequence");

    private final String value;

    AmqpBodyType(final String value) {
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
     * Creates an AmqpBodyType from its display value.
     *
     * @param value The string value of the AmqpBodyType.
     * @return The AmqpBodyType represented by the value.
     * @throws IllegalArgumentException If a AmqpBodyType cannot be parsed from the string value.
     */
    public static AmqpBodyType fromString(final String value) {
        for (AmqpBodyType bodyType : values()) {
            if (bodyType.value.equalsIgnoreCase(value)) {
                return bodyType;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "Could not convert %s to a AmqpBodyType", value));
    }
}
