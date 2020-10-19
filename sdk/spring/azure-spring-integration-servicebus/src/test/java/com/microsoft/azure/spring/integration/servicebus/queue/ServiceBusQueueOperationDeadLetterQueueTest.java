// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.queue;

import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.test.support.pojo.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceBusQueueOperationDeadLetterQueueTest
        extends ServiceBusQueueOperationSendSubscribeTest {

    private String payload = "payload";
    private User user = new User(payload);
    private Map<String, Object> headers = new HashMap<>();
    private Message<User> userMessage;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        headers.put(AzureHeaders.LOCK_TOKEN, UUID.randomUUID());
        userMessage = new GenericMessage<>(user, headers);
    }

    @Test
    public void testSendDeadLetterQueueWithManualCheckpointModel() {
        sendSubscribeOperation.sendAsync(destination, userMessage);
        sendSubscribeOperation.deadLetter(destination, userMessage, "deadletterqueue",
                "deadletterqueueexception");
        verifyDeadLetterQueueSuccessCalled(1);
    }

    @Test
    public void testSendDeadLetterQueue() {
        sendSubscribeOperation.sendAsync(destination, userMessage);
        sendSubscribeOperation.abandon(destination, userMessage);
        verifyAbandonCalled(1);
    }

    private void verifyDeadLetterQueueSuccessCalled(int times) {
        try {
            verify(this.queueClient, times(times)).deadLetter(any(), anyString(), anyString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ServiceBusException e) {
            e.printStackTrace();
        }
    }

    private void verifyAbandonCalled(int times) {
        try {
            verify(this.queueClient, times(times)).abandon(any());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ServiceBusException e) {
            e.printStackTrace();
        }

    }

    private void whenRegisterMessageHandler(IQueueClient queueClient) {
        try {
            doNothing().when(queueClient).registerMessageHandler(isA(IMessageHandler.class));
        } catch (InterruptedException | ServiceBusException e) {
            fail("Exception should not throw" + e);
        }
    }
}
