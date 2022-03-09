// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder;

import com.azure.spring.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;

/**
 * Azure ServiceBus Queue Binder Tests, test request reject.
 */
public class ServiceBusQueueRequeueRejectedBinderTests extends
    ServiceBusQueuePartitionBinderTests {

    @Override
    protected ExtendedConsumerProperties<ServiceBusConsumerProperties> createConsumerProperties() {

        ServiceBusConsumerProperties serviceBusConsumerProperties = new ServiceBusConsumerProperties();
        serviceBusConsumerProperties.setRequeueRejected(true);

        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties = new ExtendedConsumerProperties<>(
                serviceBusConsumerProperties);
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }

}
