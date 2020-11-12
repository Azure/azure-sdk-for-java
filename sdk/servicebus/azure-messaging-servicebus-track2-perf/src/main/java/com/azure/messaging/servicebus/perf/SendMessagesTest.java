// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Performance test that sends a batch of messages using both the async and synchronous message senders.
 */
public class SendMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    private final List<ServiceBusMessage> messages;
    private final ServiceBusSenderClient sender;
    private final ServiceBusSenderAsyncClient senderAsync;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public SendMessagesTest(ServiceBusStressOptions options) {
        super(options);

        final ClientLogger logger = new ClientLogger(SendMessageTest.class);
        final String queueName = getQueueName();

        logger.info("Sending {} messages to '{}'", options.getCount(), queueName);

        messages = getMessages(options.getCount());
        sender = getBuilder()
            .sender()
            .queueName(queueName)
            .buildClient();
        senderAsync = getBuilder()
            .sender()
            .queueName(queueName)
            .buildAsyncClient();
    }

    @Override
    public void run() {
        sender.sendMessages(messages);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessages(messages);
    }
}
