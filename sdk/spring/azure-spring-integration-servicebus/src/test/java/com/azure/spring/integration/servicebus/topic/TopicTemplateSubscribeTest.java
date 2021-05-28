// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.topic;

import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.support.ServiceBusProcessorClientWrapper;
import com.azure.spring.integration.test.support.SubscribeByGroupOperationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TopicTemplateSubscribeTest extends SubscribeByGroupOperationTest<ServiceBusTopicOperation> {

    @Mock
    private ServiceBusTopicClientFactory mockClientFactory;
    private ServiceBusProcessorClientWrapper processorClientWrapper;

    @Before
    public void setUp() {
        this.processorClientWrapper = new ServiceBusProcessorClientWrapper();
        ServiceBusProcessorClientWrapper anotherProcessorClientWrapper = new ServiceBusProcessorClientWrapper();

        this.subscribeByGroupOperation = new ServiceBusTopicTemplate(mockClientFactory,
                                                                     new ServiceBusMessageConverter());
        when(this.mockClientFactory.getOrCreateProcessor(eq(this.destination),
                                                         eq(this.consumerGroup),
                                                         any(),
                                                         any())).thenReturn(this.processorClientWrapper.getClient());
        when(this.mockClientFactory.getOrCreateProcessor(eq(this.destination),
                                                         eq(this.anotherConsumerGroup),
                                                         any(),
                                                         any())).thenReturn(anotherProcessorClientWrapper.getClient());
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce()).getOrCreateProcessor(eq(this.destination),
                                                                           eq(this.consumerGroup),
                                                                           any(),
                                                                           any());
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never()).getOrCreateProcessor(eq(this.destination),
                                                                     eq(this.consumerGroup),
                                                                     any(),
                                                                     any());
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        final int initTimes = this.processorClientWrapper.getInitTimes();
        if (initTimes != times) {
            fail("Expected times: " + times + ", actual times: " + initTimes);
        }
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {
    }

}
