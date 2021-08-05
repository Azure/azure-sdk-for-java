// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;


import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Performance test.
 */
public class ReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ClientLogger logger = new ClientLogger(ReceiveAndLockMessageTest.class);
    private final ServiceBusStressOptions options;
    private final String messageContent;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ReceiveAndLockMessageTest(ServiceBusStressOptions options) {
        super(options, ServiceBusReceiveMode.PEEK_LOCK);
        this.options = options;
        this.messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());
    }

    @Override
    public Mono<Void> setupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        return Mono.defer(() -> {
            int total = options.getMessagesToSend() * TOTAL_MESSAGE_MULTIPLIER;

            List<ServiceBusMessage> messages = new ArrayList<>();
            for (int i = 0; i < total; ++i) {
                ServiceBusMessage message =  new ServiceBusMessage(messageContent);
                message.setMessageId(UUID.randomUUID().toString());
                messages.add(message);
            }
            return senderAsync.sendMessages(messages);
        });
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessage> messages = receiver
            .receiveMessages(options.getMessagesToReceive());

        int count = 0;
        for (ServiceBusReceivedMessage message : messages) {
            receiver.complete(message);
            ++count;
        }

        if (count <= 0) {
            throw logger.logExceptionAsWarning(new RuntimeException("Error. Should have received some messages."));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return receiverAsync
            .receiveMessages()
            .take(options.getMessagesToReceive())
            .flatMap(message -> {
                return receiverAsync.complete(message).thenReturn(true);
            }, 1).then();
    }
}
