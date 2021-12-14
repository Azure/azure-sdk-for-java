// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import org.springframework.core.NestedRuntimeException;

/**
 * The Azure Service Bus specific {@link NestedRuntimeException}.
 */
public final class ServiceBusRuntimeException extends NestedRuntimeException {

    /**
     * Construct {@code ServiceBusRuntimeException} with the specified detail message.
     * @param msg the exception information.
     */
    public ServiceBusRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Construct {@code ServiceBusRuntimeException} with the specified detail message and nested exception.
     * @param msg the specified detail message.
     * @param cause the nested exception.
     */
    public ServiceBusRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
