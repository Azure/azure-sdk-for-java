// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.microsoft.azure.messaging.servicebus.perf.core.ServiceTest;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Performance test.
 */
public class ReceiveAndDeleteMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ClientLogger logger = new ClientLogger(ReceiveAndDeleteMessageTest.class);
    private final ServiceBusStressOptions options;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ReceiveAndDeleteMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.RECEIVEANDDELETE);
        this.options = options;
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return Mono.fromFuture(CompletableFuture.allOf(sender.closeAsync(), receiver.closeAsync()))
            .then(super.globalCleanupAsync());
    }

    private Mono<Void> sendMessage() {
        return Mono.fromCallable(() -> {
            int total = options.getParallel() * options.getMessagesToSend() * TOTAL_MESSAGE_MULTIPLIER;

            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < total; ++i) {
                Message message = new Message(CONTENTS);
                message.setMessageId(UUID.randomUUID().toString());
                messages.add(message);
            }

            return sender.sendBatchAsync(messages).get();
        });
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        return super.globalSetupAsync()
            .then(sendMessage());
    }

    @Override
    public void run() {
        Collection<IMessage> messages = null;
        try {
            messages = receiver.receiveBatch(options.getMessagesToReceive());
            if (messages.size() <= 0) {
                throw logger.logExceptionAsWarning(new RuntimeException("Error. Should have received some messages."));
            }
        } catch (Exception e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromFuture(receiver.receiveBatchAsync(options.getMessagesToReceive()))
            .handle((messages, synchronousSink) -> {
                int count = messages.size();
                logger.verbose(" Async received  size of received : {}", count);

                if (count <= 0) {
                    synchronousSink.error(new RuntimeException("Error. Should have received some messages."));
                }
                synchronousSink.complete();
            });
    }
}
