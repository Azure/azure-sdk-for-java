// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.servicebus.perf.core.ServiceBusStressOptions;
import com.microsoft.azure.servicebus.perf.core.ServiceTest;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Performance test.
 */
public class SendMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final Message message;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public SendMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEKLOCK);
        String messageId = UUID.randomUUID().toString();
        message = new Message(TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend()));
        message.setMessageId(messageId);
    }

    @Override
    public void run() {
        try {
            sender.send(message);
        } catch (InterruptedException | ServiceBusException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromFuture(sender.sendAsync(message));
    }
}
