// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Error response codes returned from AMQP.
 */
public enum AmqpResponseCode {
    ACCEPTED(0xca),
    OK(200),
    BAD_REQUEST(400),
    NOT_FOUND(0x194),
    FORBIDDEN(0x193),
    INTERNAL_SERVER_ERROR(500),
    UNAUTHORIZED(0x191);

    private static Map<Integer, AmqpResponseCode> valueMap = new HashMap<>();

    static {
        for (AmqpResponseCode code : AmqpResponseCode.values()) {
            valueMap.put(code.value, code);
        }
    }

    private final int value;

    AmqpResponseCode(final int value) {
        this.value = value;
    }

    /**
     * Creates an AmqpResponseCode for the provided integer {@code value}.
     *
     * @param value The integer value representing an error code.
     * @return The corresponding AmqpResponseCode for the provided value, or {@code null} if no matching response code
     * is found.
     */
    public static AmqpResponseCode fromValue(final int value) {
        return valueMap.get(value);
    }

    /**
     * Gets the integer value of the AmqpResponseCode
     *
     * @return The integer value of the AmqpResponseCode
     */
    public int getValue() {
        return this.value;
    }
}
