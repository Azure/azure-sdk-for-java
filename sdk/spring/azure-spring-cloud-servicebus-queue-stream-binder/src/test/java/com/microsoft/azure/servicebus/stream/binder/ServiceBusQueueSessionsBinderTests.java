/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;

/**
 * Test cases are defined in super class
 *
 * @author Eduardo Sciullo
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceBusQueueSessionsBinderTests extends
        ServiceBusQueuePartitionBinderTests {

    @Override
    protected ExtendedConsumerProperties<ServiceBusConsumerProperties> createConsumerProperties() {

        ServiceBusConsumerProperties serviceBusConsumerProperties = new ServiceBusConsumerProperties();
        serviceBusConsumerProperties.setSessionsEnabled(true);

        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties = new ExtendedConsumerProperties<>(
                serviceBusConsumerProperties);
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }

}
