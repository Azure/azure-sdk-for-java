// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import java.util.HashMap;
import java.util.Map;

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

    public static AmqpResponseCode valueOf(final int value) {
        return valueMap.get(value);
    }

    public int getValue() {
        return this.value;
    }
}
