/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.util.HashMap;
import java.util.Map;

public enum AmqpResponseCode
{
    ACCEPTED (0xca),
    OK (200),
    BAD_REQUEST (400),
    NOT_FOUND (0x194),
    FORBIDDEN (0x193),
    INTERNAL_SERVER_ERROR (500),
    UNAUTHORIZED (0x191);
    
    private final int value;
    
    private static Map<Integer, AmqpResponseCode> valueMap = new HashMap<>();
    
    static {
        for (AmqpResponseCode code: AmqpResponseCode.values()) {
            valueMap.put(code.value, code);
        }
    }
    
    private AmqpResponseCode(final int value)
    {
        this.value = value;
    }
    
    public int getValue()
    {
        return this.value;
    }
    
    public static AmqpResponseCode valueOf(final int value) {
        return valueMap.get(value);
    }
}
