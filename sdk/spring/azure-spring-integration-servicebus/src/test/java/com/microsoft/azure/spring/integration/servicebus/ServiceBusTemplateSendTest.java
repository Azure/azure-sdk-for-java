// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusSenderFactory;
import com.microsoft.azure.spring.integration.test.support.SendOperationTest;
import org.junit.Before;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

public abstract class ServiceBusTemplateSendTest<T extends ServiceBusSenderFactory, C extends IMessageSender>
    extends SendOperationTest<SendOperation> {

    protected T mockClientFactory;

    protected C mockClient;

    @Before
    public abstract void setUp();

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockClient, times(times)).sendAsync(isA(IMessage.class));
    }

    @Override
    protected void verifyPartitionSenderCalled(int times) {
        verifySendCalled(times);
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getOrCreateSender(anyString())).thenThrow(ServiceBusRuntimeException.class);
    }

    @Override
    protected void verifyGetClientCreator(int times) {
        verify(this.mockClientFactory, times(times)).getOrCreateSender(anyString());
    }

    @Override
    protected void verifySendWithPartitionKey(int times) {
        verifySendCalled(times);
    }

    @Override
    protected void verifySendWithPartitionId(int times) {
        verifySendCalled(times);
    }

}
