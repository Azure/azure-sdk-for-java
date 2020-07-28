/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus;

import org.springframework.core.NestedRuntimeException;

/**
 * The Azure Service Bus specific {@link NestedRuntimeException}.
 *
 * @author Warren Zhu
 */
public class ServiceBusRuntimeException extends NestedRuntimeException {

    public ServiceBusRuntimeException(String msg) {
        super(msg);
    }

    public ServiceBusRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
