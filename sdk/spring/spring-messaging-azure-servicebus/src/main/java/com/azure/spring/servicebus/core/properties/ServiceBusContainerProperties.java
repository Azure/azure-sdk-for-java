// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.service.servicebus.processor.ServiceBusMessageListener;
import com.azure.spring.service.servicebus.processor.consumer.ServiceBusProcessorErrorContextConsumer;

/**
 * The properties to describe a Service Bus listener container.
 */
public class ServiceBusContainerProperties extends ProcessorProperties {

    private ServiceBusMessageListener messageListener;
    private ServiceBusProcessorErrorContextConsumer errorContextConsumer;

    public ServiceBusMessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(ServiceBusMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public ServiceBusProcessorErrorContextConsumer getErrorContextConsumer() {
        return errorContextConsumer;
    }

    public void setErrorContextConsumer(ServiceBusProcessorErrorContextConsumer errorContextConsumer) {
        this.errorContextConsumer = errorContextConsumer;
    }
}
