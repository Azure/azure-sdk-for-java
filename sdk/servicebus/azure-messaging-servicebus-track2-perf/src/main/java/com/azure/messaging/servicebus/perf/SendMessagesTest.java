// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Performance test that sends a batch of messages using both the async and synchronous message senders.
 */
public class SendMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    private final List<ServiceBusMessage> messages;
    private final ServiceBusSenderClient sender;
    private final ServiceBusSenderAsyncClient senderAsync;

    /**
     * Creates an instance of {@link SendMessagesTest}.
     *
     * @param options to set performance test options.
     */
    public SendMessagesTest(ServiceBusStressOptions options) {
        super(options, new ClientLogger(SendMessagesTest.class));

        final String queueName = getQueueName();

        getLogger().info("Sending {} messages to '{}'.", options.getCount(), queueName);

        this.messages = getMessages(options.getCount());

        if (options.isSync()) {
            this.sender = getBuilder()
                .sender()
                .queueName(queueName)
                .buildClient();
            this.senderAsync = null;
        } else {
            this.sender = null;
            this.senderAsync = getBuilder()
                .sender()
                .queueName(queueName)
                .buildAsyncClient();
        }
    }

    @Override
    public void run() {
        sender.sendMessages(messages);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessages(messages);
    }

    @Override
    public Mono<Void> cleanupAsync() {
        dispose(sender, senderAsync);
        return Mono.empty();
    }
}
