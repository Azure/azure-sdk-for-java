// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Performance test that sends a batch of messages using both the async and synchronous methods.
 */
public class SendMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    private final List<IMessage> messages;
    private final IMessageSender sender;

    /**
     * Instantiates an instance of the test class.
     *
     * @param options to set performance test options.
     *
     * @throws RuntimeException if the messaging entity cannot be created.
     */
    public SendMessagesTest(ServiceBusStressOptions options) {
        super(options, new ClientLogger(SendMessagesTest.class));

        final String queueName = getQueueName();

        getLogger().info("Sending {} messages to '{}'.", options.getCount(), queueName);

        this.messages = getMessages(options.getCount());

        try {
            this.sender = ClientFactory.createMessageSenderFromEntityPath(getMessagingFactory(), queueName);
        } catch (ServiceBusException | InterruptedException e) {
            throw getLogger().logExceptionAsError(new RuntimeException("Unable to create sender.", e));
        }
    }

    @Override
    public void run() {
        try {
            sender.sendBatch(messages);
        } catch (InterruptedException | ServiceBusException e) {
            throw getLogger().logExceptionAsWarning(new RuntimeException("Unable to send messages synchronously.", e));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromFuture(sender.sendBatchAsync(messages));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromFuture(getMessagingFactory().closeAsync());
    }
}
