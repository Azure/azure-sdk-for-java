/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

public enum AmqpManagementResponseCode
{
    ACCEPTED (0xca),
    OK (200);
    
    private final int value;
    private AmqpManagementResponseCode(final int value)
    {
        this.value = value;
    }
    
    public int getValue()
    {
        return this.value;
    }
}
