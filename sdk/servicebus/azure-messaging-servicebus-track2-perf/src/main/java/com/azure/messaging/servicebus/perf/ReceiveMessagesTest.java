// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * Receives messages using various modes and settles them if set.
 */
public class ReceiveMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    /**
     * The number of iterations we expect during to run per second during warm-up.
     */
    private static final int WARM_UP_MULTIPLIER = 5;

    private final String queueName;
    private final ServiceBusReceiverClient receiver;
    private final ServiceBusReceiverAsyncClient receiverAsync;

    /**
     * Creates an instance of {@link ReceiveMessagesTest}.
     *
     * @param options to set performance test options.
     */
    public ReceiveMessagesTest(ServiceBusStressOptions options) {
        super(options, new ClientLogger(ReceiveMessagesTest.class));

        this.queueName = getQueueName();

        // Disabling auto complete because the track 1 receiver does not have auto complete or auto lock renewal
        // for the low level receiver. Only the processor does this.
        final ServiceBusReceiverClientBuilder builder = getBuilder()
            .receiver()
            .queueName(queueName)
            .maxAutoLockRenewDuration(Duration.ZERO)
            .disableAutoComplete();

        if (options.getReceiveMode() != null) {
            builder.receiveMode(options.getReceiveMode());
        }

        if (options.isSync()) {
            this.receiver = builder.buildClient();
            this.receiverAsync = null;
        } else {
            this.receiver = null;
            this.receiverAsync = builder.buildAsyncClient();
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

        final List<ServiceBusMessage> messages = getMessages(totalMessages);

        getLogger().info("Sending {} messages to '{}'.", messages.size(), queueName);

        return Mono.using(
            () -> getBuilder().sender().queueName(queueName).buildAsyncClient(),
            sender -> sender.sendMessages(messages),
            sender -> sender.close());
    }

    @Override
    public void run() {
        final int numberOfMessages = options.getCount();

        getLogger().info("Receiving {} messages from '{}' synchronously.", numberOfMessages, queueName);
        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(numberOfMessages);

        int count = 0;
        for (ServiceBusReceivedMessage message : messages) {
            count++;

            if (options.isSettleMessage()) {
                receiver.complete(message);
            }
        }

        if (count != numberOfMessages) {
            throw getLogger().logExceptionAsError(new RuntimeException(String.format(
                "Should have received %s messages. Received: %s", numberOfMessages, count)));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        final int numberOfMessages = options.getCount();
        getLogger().info("Receiving {} messages from '{}' asynchronously.", numberOfMessages, queueName);

        return receiverAsync.receiveMessages()
            .take(options.getCount())
            .flatMap(message -> options.isSettleMessage() ? receiverAsync.complete(message) : Mono.empty(),
                options.getParallel(), options.getCount())
            .then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        dispose(receiver, receiverAsync);
        return Mono.empty();
    }
}
