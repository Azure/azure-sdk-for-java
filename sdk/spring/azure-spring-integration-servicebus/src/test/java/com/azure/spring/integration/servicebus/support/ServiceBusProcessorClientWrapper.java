// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.support;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class ServiceBusProcessorClientWrapper {

    private int initTimes;
    private final ServiceBusProcessorClient processorClient;
    private boolean started;

    public ServiceBusProcessorClientWrapper() {
        this.processorClient = mock(ServiceBusProcessorClient.class);
        this.started = false;
        this.initTimes = 0;
        mockProcessorStartMethod();
    }

    public ServiceBusProcessorClient getClient() {
        return processorClient;
    }

    public int getInitTimes() {
        return initTimes;
    }

    private void mockProcessorStartMethod() {
        Answer<Void> answer = invocationOnMock -> {
            if (!started) {
                started = true;
                initTimes++;
            }
            return null;
        };
        doAnswer(answer).when(this.processorClient).start();
    }
}
