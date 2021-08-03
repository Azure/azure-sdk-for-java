// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.servicebus.factory.ServiceBusSenderFactory;
import com.azure.spring.integration.test.support.SendOperationTest;
import org.junit.jupiter.api.BeforeEach;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases to test service bus send operations.
 *
 * @param <T>
 * @param <C>
 */
public abstract class ServiceBusTemplateSendTest<T extends ServiceBusSenderFactory,
                                                    C extends ServiceBusSenderAsyncClient>
    extends SendOperationTest<SendOperation> {

    protected C mockClient;
    protected T mockClientFactory;

    @BeforeEach
    public abstract void setUp();

    @Override
    protected void setupError(String errorMessage) {
        when(this.mockClient.sendMessage(isA(ServiceBusMessage.class))).thenReturn(Mono.error(new IllegalArgumentException(
            errorMessage)));
    }

    @Override
    protected void verifySendCalled(int times) {
        verify(this.mockClient, times(times)).sendMessage(isA(ServiceBusMessage.class));
    }

    @Override
    protected void verifyPartitionSenderCalled(int times) {
        verifySendCalled(times);
    }

    @Override
    protected void whenSendWithException() {
        when(this.mockClientFactory.getOrCreateSender(anyString())).thenThrow(NullPointerException.class);
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
