// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Performance test.
 */
public class ReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ClientLogger logger = new ClientLogger(ReceiveAndLockMessageTest.class);

    private List<Message> messages = new ArrayList<>();

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ReceiveAndLockMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEKLOCK);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        int totalMessageMultiplier = 50;

        String messageId = UUID.randomUUID().toString();
        Message message = new Message(CONTENTS);
        message.setMessageId(messageId);

        return Mono.defer(() -> {
            messages = IntStream.range(0, options.getMessagesToSend() * totalMessageMultiplier)
                .mapToObj(index -> message)
                .collect(Collectors.toList());

            try {
                sender.sendBatch(messages);
            } catch (InterruptedException | ServiceBusException e) {
                throw logger.logExceptionAsWarning(new RuntimeException(e));
            }
            return Mono.empty();
        });
    }

    @Override
    public void run() {
        Collection<IMessage> messages = null;
        try {
            messages = receiver.receiveBatch(options.getMessagesToReceive());
        } catch  (Exception e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }

        for (IMessage message : messages) {
            try {
                receiver.complete(message.getLockToken());
            } catch (InterruptedException | ServiceBusException e) {
                throw logger.logExceptionAsWarning(new RuntimeException(e));
            }
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromFuture(receiver.receiveBatchAsync(options.getMessagesToReceive()))
            .map(iMessages -> {
                try {
                    for (IMessage message : iMessages) {
                        receiver.complete(message.getLockToken());
                    }
                } catch (InterruptedException | ServiceBusException e) {
                    throw logger.logExceptionAsWarning(new RuntimeException(e));
                }
                return Mono.just(iMessages);
            })
            .then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        try {
            sender.close();
        } catch (ServiceBusException e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
        return Mono.empty();
    }

    /**
     * Runs the cleanup logic after the performance test finishes.
     * @return An empty {@link Mono}
     */
    public Mono<Void> globalCleanupAsync() {
        try {
            sender.close();
        } catch (ServiceBusException e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
        return Mono.empty();
    }
}
