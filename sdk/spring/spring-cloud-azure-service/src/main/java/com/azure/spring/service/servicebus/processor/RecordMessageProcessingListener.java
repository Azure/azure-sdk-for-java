// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.processor;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

/**
 * A listener to process Service Bus messages.
 */
public interface RecordMessageProcessingListener extends MessageProcessingListener {
    void onMessage(ServiceBusReceivedMessageContext messageContext);
}
