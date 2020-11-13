// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Receives messages using various modes and settles them if set.
 */
public class ReceiveMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    /**
     * The number of iterations we expect during to run per second during warm-up.
     */
    private static final int WARM_UP_MULTIPLIER = 5;

    private final String queueName;
    private final IMessageReceiver receiver;

    /**
     * Creates an instance of {@link ReceiveMessagesTest}.
     *
     * @param options to set performance test options.
     */
    public ReceiveMessagesTest(ServiceBusStressOptions options) {
        super(options, new ClientLogger(ReceiveMessagesTest.class));

        this.queueName = getQueueName();

        try {
            if (options.getReceiveMode() == null) {
                this.receiver = ClientFactory.createMessageReceiverFromEntityPath(getMessagingFactory(), queueName);
            } else {
                this.receiver = ClientFactory.createMessageReceiverFromEntityPath(getMessagingFactory(), queueName,
                    options.getReceiveMode());
            }
        } catch (ServiceBusException | InterruptedException e) {
            throw getLogger().logExceptionAsError(
                new RuntimeException("Unable to create receiver for: " + queueName));
        }

        getLogger().info("Mode: {}. AutoComplete: {}.", options.getReceiveMode(), options.isAutoComplete());
    }

    @Override
    public Mono<Void> setupAsync() {
        final int numberOfMessages = options.getCount() * options.getIterations();
        final int totalMessages;

        if (options.getWarmup() > 0) {
            final int totalIterations = WARM_UP_MULTIPLIER * options.getWarmup();

            // Have the total number of messages be the sum of messages you expect to receive during the warm up period
            // plus the number of messages from the normal run test scenario.
            totalMessages = numberOfMessages + (options.getCount() * totalIterations);
        } else {
            totalMessages = numberOfMessages;
        }

        final List<IMessage> messages = getMessages(totalMessages);

        getLogger().info("Sending {} messages to '{}'.", messages.size(), queueName);

        return Mono.using(
            () -> ClientFactory.createMessageSenderFromEntityPath(getMessagingFactory(), queueName),
            sender -> Mono.fromFuture(sender.sendBatchAsync(messages)),
            sender -> Mono.fromFuture(sender.closeAsync()));
    }

    @Override
    public void run() {
        final int numberOfMessages = options.getCount();

        final Collection<IMessage> receivedMessages;
        try {
            receivedMessages = receiver.receiveBatch(numberOfMessages);
        } catch (Exception e) {
            throw getLogger().logExceptionAsWarning(
                new RuntimeException("Unable to receive messages from: " + queueName, e));
        }

        for (IMessage message : receivedMessages) {
            try {
                receiver.complete(message.getLockToken());
            } catch (InterruptedException | ServiceBusException e) {
                throw getLogger().logExceptionAsWarning(new RuntimeException(String.format(
                    "Unable to complete message: %s. EnqueuedTime: %s.", message.getSequenceNumber(),
                    message.getEnqueuedTimeUtc()), e));
            }
        }

        if (receivedMessages.size() != numberOfMessages) {
            throw getLogger().logExceptionAsError(new RuntimeException(String.format(
                "Should have received %s messages. Received: %s", numberOfMessages, receivedMessages.size())));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        final int numberOfMessages = options.getCount();
        getLogger().info("Receiving {} messages from '{}' asynchronously.", numberOfMessages, queueName);

        return Mono.fromFuture(receiver.receiveBatchAsync(numberOfMessages)
            .thenComposeAsync(messages -> {
                return CompletableFuture.allOf(messages.stream()
                    .map(message -> options.isSettleMessage()
                        ? receiver.completeAsync(message.getLockToken())
                        : CompletableFuture.completedFuture(null))
                    .toArray(CompletableFuture[]::new));
            }));
    }
}
