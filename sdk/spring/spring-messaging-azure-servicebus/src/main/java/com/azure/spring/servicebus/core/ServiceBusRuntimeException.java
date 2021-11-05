// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import org.springframework.core.NestedRuntimeException;

public class ServiceBusRuntimeException extends NestedRuntimeException {
    public ServiceBusRuntimeException(String msg) {
        super(msg);
    }

    public ServiceBusRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
