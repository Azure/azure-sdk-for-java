// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

import java.util.function.Consumer;

/**
 *
 */
public interface ServiceBusMessageProcessor {

    Consumer<ServiceBusErrorContext> processError();

    Consumer<ServiceBusReceivedMessageContext> processMessage();

}
