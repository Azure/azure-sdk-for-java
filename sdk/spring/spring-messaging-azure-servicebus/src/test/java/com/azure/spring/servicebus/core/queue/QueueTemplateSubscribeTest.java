// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.queue;

import com.azure.spring.messaging.core.SubscribeOperationTest;
import com.azure.spring.servicebus.core.processor.ServiceBusQueueProcessorClientFactory;
import com.azure.spring.servicebus.core.sender.ServiceBusSenderClientFactory;
import com.azure.spring.servicebus.support.ServiceBusProcessorClientWrapper;
import com.azure.spring.servicebus.support.converter.ServiceBusMessageConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class QueueTemplateSubscribeTest extends SubscribeOperationTest<ServiceBusQueueOperation> {

    @Mock
    private ServiceBusQueueProcessorClientFactory mockClientFactory;
    @Mock
    private ServiceBusSenderClientFactory mockSenderClientFactory;
    private ServiceBusProcessorClientWrapper processorClientWrapper;

    private AutoCloseable closeable;


    @BeforeEach
    public void setUp() {
        this.closeable = MockitoAnnotations.openMocks(this);
        this.processorClientWrapper = new ServiceBusProcessorClientWrapper();
        this.subscribeOperation = new ServiceBusQueueTemplate(mockSenderClientFactory, mockClientFactory, new ServiceBusMessageConverter());
        when(this.mockClientFactory.createProcessor(eq(this.destination), any())).thenReturn(
            processorClientWrapper.getClient());
    }

    @AfterEach
    public void close() throws Exception {
        closeable.close();
    }

    @Override
    protected void verifySubscriberCreatorCalled() {
        verify(this.mockClientFactory, atLeastOnce()).createProcessor(anyString(), any());
    }

    @Override
    protected void verifySubscriberCreatorNotCalled() {
        verify(this.mockClientFactory, never()).createProcessor(anyString(), any());
    }

    @Override
    protected void verifySubscriberRegistered(int times) {
        final int initTimes = this.processorClientWrapper.getInitTimes();
        if (initTimes != times) {
            fail("Expect times: " + times + " and actual times: " + initTimes);
        }
    }

    @Override
    protected void verifySubscriberUnregistered(int times) {

    }

}
