// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;

import org.springframework.core.NestedRuntimeException;

/**
 * The Azure Service Bus specific {@link NestedRuntimeException}.
 *
 * @author Warren Zhu
 */
public class ServiceBusRuntimeException extends NestedRuntimeException {

    /**
     *
     * @param msg The message.
     */
    public ServiceBusRuntimeException(String msg) {
        super(msg);
    }

    /**
     *
     * @param msg The message.
     * @param cause The cause of exception.
     */
    public ServiceBusRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
