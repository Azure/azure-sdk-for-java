// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.processor;

import com.azure.spring.service.servicebus.processor.consumer.ServiceBusErrorContextConsumer;

/**
 * A listener to process Service Bus messages.
 */
public interface MessageProcessingListener {

    default ServiceBusErrorContextConsumer getErrorContextConsumer() {
        return errorContext -> { };
    }

}
