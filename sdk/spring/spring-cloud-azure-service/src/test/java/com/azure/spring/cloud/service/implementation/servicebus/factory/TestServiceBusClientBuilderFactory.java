// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;

import static org.mockito.Mockito.mock;

public class TestServiceBusClientBuilderFactory extends ServiceBusClientBuilderFactory {
    public TestServiceBusClientBuilderFactory(AzureProperties azureProperties) {

        super((ServiceBusClientCommonProperties)azureProperties);
    }

    @Override
    public ServiceBusClientBuilder createBuilderInstance() {
        return mock(ServiceBusClientBuilder.class);
    }

}
