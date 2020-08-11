// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.microsoft.azure.servicebus.stream.binder.test.AzurePartitionBinderTests;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusQueueClientFactory;
import com.microsoft.azure.spring.integration.servicebus.queue.support.ServiceBusQueueTestOperation;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.HeaderMode;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test cases are defined in super class
 *
 * @author Warren Zhu
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceBusQueuePartitionBinderTests extends
        AzurePartitionBinderTests<ServiceBusQueueTestBinder, ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                ExtendedProducerProperties<ServiceBusProducerProperties>> {
    @Mock
    ServiceBusQueueClientFactory clientFactory;

    @Mock
    QueueClient queueClient;

    private ServiceBusQueueTestBinder binder;

    @Before
    public void setUp() {
        when(this.clientFactory.getOrCreateClient(anyString())).thenReturn(this.queueClient);
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.complete(null);
        when(this.queueClient.completeAsync(any())).thenReturn(future);
        this.binder = new ServiceBusQueueTestBinder(new ServiceBusQueueTestOperation(this.clientFactory));
    }

    @Override
    protected String getClassUnderTestName() {
        return ServiceBusQueueTestBinder.class.getSimpleName();
    }

    @Override
    protected ServiceBusQueueTestBinder getBinder() throws Exception {
        return this.binder;
    }

    @Override
    protected ExtendedConsumerProperties<ServiceBusConsumerProperties> createConsumerProperties() {
        ExtendedConsumerProperties<ServiceBusConsumerProperties> properties =
                new ExtendedConsumerProperties<>(new ServiceBusConsumerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }

    @Override
    protected ExtendedProducerProperties<ServiceBusProducerProperties> createProducerProperties() {
        ExtendedProducerProperties<ServiceBusProducerProperties> properties =
                new ExtendedProducerProperties<>(new ServiceBusProducerProperties());
        properties.setHeaderMode(HeaderMode.embeddedHeaders);
        return properties;
    }

    @Override
    public void testOneRequiredGroup() {
        // Required group test rely on unsupported start position of consumer properties
    }

    @Override
    public void testTwoRequiredGroups() {
        // Required group test rely on unsupported start position of consumer properties
    }
}
