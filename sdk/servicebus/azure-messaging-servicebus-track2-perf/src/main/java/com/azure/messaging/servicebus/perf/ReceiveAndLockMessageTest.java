// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Performance test.
 */
public class ReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ReceiveAndLockMessageTest.class);
    private final ServiceBusStressOptions options;
    private final String messageContent;
    private final Duration testDuration;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ReceiveAndLockMessageTest(ServiceBusStressOptions options) {
        super(options, ServiceBusReceiveMode.PEEK_LOCK);
        this.options = options;
        this.messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());
        this.testDuration = Duration.ofSeconds(options.getDuration() - 1);
    }

    @Override
    public Mono<Void> setupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        return Mono.defer(() -> {
            int total = options.getMessagesToSend() * TOTAL_MESSAGE_MULTIPLIER;

            List<ServiceBusMessage> messages = new ArrayList<>();
            for (int i = 0; i < total; ++i) {
                ServiceBusMessage message = new ServiceBusMessage(messageContent);
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
            throw LOGGER.logExceptionAsWarning(new RuntimeException("Error. Should have received some messages."));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        final Mono<ServiceBusReceiverAsyncClient> receiverAsyncMono = Mono.defer(() -> Mono.just(baseBuilder
            .receiver()
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .queueName(queueName)
            .buildAsyncClient()));

        final AtomicInteger messagesReceived = new AtomicInteger();
        final AtomicReference<ServiceBusReceiverAsyncClient> receiverClient = new AtomicReference<>();

        return receiverAsyncMono
            .flatMap(serviceBusReceiverAsyncClient -> {
                receiverClient.set(serviceBusReceiverAsyncClient);
                return serviceBusReceiverAsyncClient
                    .receiveMessages()
                    .take(testDuration)
                    .flatMap(m -> {
                        messagesReceived.incrementAndGet();
                        return Mono.just(m);
                    }, 1)
                    .doFinally(signal -> {
                        receiverClient.get().close();
                        updateResults( this.getClass().getName(), messagesReceived.get(), testDuration);
                        LOGGER.verbose("Exit from receive.");
                    })
                    .then();
            });
    }
}
