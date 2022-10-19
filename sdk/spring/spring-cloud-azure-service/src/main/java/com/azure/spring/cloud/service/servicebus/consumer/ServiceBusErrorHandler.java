// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.servicebus.consumer;

import com.azure.messaging.servicebus.ServiceBusErrorContext;

import java.util.function.Consumer;

/**
 * The error handler to handle errors when listening to Service Bus.
 */
@FunctionalInterface
public interface ServiceBusErrorHandler extends Consumer<ServiceBusErrorContext> {

}
