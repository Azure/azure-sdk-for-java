// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;

import org.junit.Test;

public class MessageLockAutoRenewTaskTests {
    @Test
    public void runAttemptsToRenewMessageLock() throws InterruptedException, ServiceBusException {
        UUID messageId = UUID.randomUUID();
        IMessage message = mock(IMessage.class);
        IMessageReceiver mockReceiver = mock(IMessageReceiver.class);

        when(message.getLockToken()).thenReturn(messageId);
        
        MessageLockAutoRenewTask sut = new MessageLockAutoRenewTask(mockReceiver, message);

        sut.run();

        verify(mockReceiver).renewMessageLock(messageId);
    }

}
