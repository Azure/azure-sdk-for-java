// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.service.servicebus.processor.ServiceBusMessageListener;

import java.util.function.Consumer;

/**
 * The properties to describe a Service Bus listener container.
 */
public class ServiceBusContainerProperties extends ProcessorProperties {

    private ServiceBusMessageListener messageListener;
    private Consumer<ServiceBusErrorContext> errorContextConsumer;

    public ServiceBusMessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(ServiceBusMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public Consumer<ServiceBusErrorContext> getErrorContextConsumer() {
        return errorContextConsumer;
    }

    public void setErrorContextConsumer(Consumer<ServiceBusErrorContext> errorContextConsumer) {
        this.errorContextConsumer = errorContextConsumer;
    }
}
