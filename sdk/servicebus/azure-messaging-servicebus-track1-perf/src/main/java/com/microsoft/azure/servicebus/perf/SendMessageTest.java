// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

/**
 * Performance test that sends a single message using both the async and synchronous methods.
 */
public class SendMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final IMessage message;
    private final IMessageSender sender;

    /**
     * Instantiates an instance of the test class.
     *
     * @param options to set performance test options.
     *
     * @throws RuntimeException if the messaging entity cannot be created.
     */
    public SendMessageTest(ServiceBusStressOptions options) {
        super(options, new ClientLogger(SendMessageTest.class));

        final String queueName = getQueueName();

        getLogger().info("Sending 1 message to '{}'.", queueName);

        this.message = getMessages(1).get(0);

        try {
            this.sender = ClientFactory.createMessageSenderFromEntityPath(getMessagingFactory(), queueName);
        } catch (ServiceBusException | InterruptedException e) {
            throw getLogger().logExceptionAsError(new RuntimeException("Unable to create sender.", e));
        }
    }

    @Override
    public void run() {
        try {
            sender.send(message);
        } catch (InterruptedException | ServiceBusException e) {
            throw getLogger().logExceptionAsWarning(new RuntimeException("Unable to send message synchronously.", e));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromFuture(sender.sendAsync(message));
    }
}
