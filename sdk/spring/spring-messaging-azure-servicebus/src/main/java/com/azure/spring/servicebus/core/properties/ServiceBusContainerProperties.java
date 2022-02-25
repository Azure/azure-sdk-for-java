// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.service.servicebus.consumer.ServiceBusMessageListener;

/**
 * The properties to describe a Service Bus listener container.
 */
public class ServiceBusContainerProperties extends ProcessorProperties {

    private ServiceBusMessageListener messageListener;
    private ServiceBusErrorHandler errorContextConsumer;

    public ServiceBusMessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(ServiceBusMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public ServiceBusErrorHandler getErrorContextConsumer() {
        return errorContextConsumer;
    }

    public void setErrorContextConsumer(ServiceBusErrorHandler errorContextConsumer) {
        this.errorContextConsumer = errorContextConsumer;
    }
}
