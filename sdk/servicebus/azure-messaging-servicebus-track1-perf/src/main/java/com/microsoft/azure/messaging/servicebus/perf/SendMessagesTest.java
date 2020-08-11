// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.microsoft.azure.messaging.servicebus.perf.core.ServiceTest;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Performance test.
 */
public class SendMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    private final ClientLogger logger = new ClientLogger(SendMessagesTest.class);
    private final ServiceBusStressOptions options;

    private List<IMessage> messages = new ArrayList<>();

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public SendMessagesTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEKLOCK);
        this.options = options;
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(CONTENTS);
        message.setMessageId(messageId);

        return Mono.defer(() -> {
            messages = IntStream.range(0, options.getMessagesToSend())
            .mapToObj(index -> message)
            .collect(Collectors.toList());
            return Mono.empty();
        });
    }

    @Override
    public void run() {
        try {
            sender.sendBatch(messages);
        } catch (InterruptedException | ServiceBusException e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        try {
            sender.sendBatchAsync(messages).get();
        } catch (InterruptedException | ExecutionException e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
        return Mono.empty();
    }
}
