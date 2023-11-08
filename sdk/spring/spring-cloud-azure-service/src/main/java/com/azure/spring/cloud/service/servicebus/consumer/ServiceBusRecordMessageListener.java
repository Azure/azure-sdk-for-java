// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.servicebus.consumer;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.cloud.service.listener.MessageListener;

/**
 * A listener to process Service Bus messages.
 */
@FunctionalInterface
public interface ServiceBusRecordMessageListener extends MessageListener<ServiceBusReceivedMessageContext> {


}
