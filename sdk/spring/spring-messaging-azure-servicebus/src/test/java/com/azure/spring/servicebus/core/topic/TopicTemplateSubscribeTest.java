// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.topic;

import com.azure.spring.servicebus.core.processor.ServiceBusNamespaceTopicProcessorClientFactory;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import com.azure.spring.servicebus.support.ServiceBusProcessorClientWrapper;
import com.azure.spring.messaging.core.SubscribeByGroupOperationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TopicTemplateSubscribeTest extends SubscribeByGroupOperationTest<ServiceBusTopicOperation> {

    @Mock
    private ServiceBusNamespaceTopicProcessorClientFactory mockClientFactory;
    private ServiceBusProcessorClientWrapper processorClientWrapper;
    private AutoCloseable closeable;
    @BeforeEach
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.processorClientWrapper = new ServiceBusProcessorClientWrapper();
        ServiceBusProcessorClientWrapper anotherProcessorClientWrapper = new ServiceBusProcessorClientWrapper();

        this.subscribeByGroupOperation = new ServiceBusTopicTemplate(mockClientFactory, new ServiceBusMessageConverter());
        when(this.mockClientFactory.createProcessor(eq(this.destination),
                                                         eq(this.consumerGroup),
                                                         any(),
                                                         any())).thenReturn(this.processorClientWrapper.getClient());
        when(this.mockClientFactory.createProcessor(eq(this.destination),
                                                         eq(this.anotherConsumerGroup),
                                                         any(),
                                                         any())).thenReturn(anotherProcessorClientWrapper.getClient());
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce()).createProcessor(eq(this.destination),
                                                                           eq(this.consumerGroup),
                                                                           any(),
                                                                           any());
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never()).createProcessor(eq(this.destination),
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
