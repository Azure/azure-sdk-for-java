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
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Performance test.
 */
public class ReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ClientLogger logger = new ClientLogger(ReceiveAndLockMessageTest.class);
    private final ServiceBusStressOptions options;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ReceiveAndLockMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEKLOCK);
        this.options = options;
    }

    private Mono<Void> sendMessage() {
        return Mono.defer(() -> {
            int total = options.getParallel() * options.getMessagesToSend() * TOTAL_MESSAGE_MULTIPLIER;
            List<Message> messages = new ArrayList<>();
            for (int i = 0; i < total; ++i) {
                Message message = new Message(CONTENTS);
                message.setMessageId(UUID.randomUUID().toString());
                messages.add(message);
            }
            return Mono.fromFuture(sender.sendBatchAsync(messages));
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
        return Mono.fromFuture(receiver.receiveBatchAsync(options.getMessagesToReceive())
            .thenAccept(iMessages -> {
                List<CompletableFuture<Void>> completeMessagesList = new ArrayList<>();
                for (IMessage message : iMessages) {
                    completeMessagesList.add(receiver.completeAsync(message.getLockToken()));
                }

                try {
                    final int size =  completeMessagesList.size();
                    CompletableFuture.allOf(completeMessagesList.toArray(new CompletableFuture<?>[size])).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw logger.logExceptionAsWarning(new RuntimeException(e));
                }
            }));
    }

    /**
     * Runs the cleanup logic after the performance test finishes.
     * @return An empty {@link Mono}
     */
    @Override
    public Mono<Void> globalCleanupAsync() {
        return Mono.when(Mono.fromFuture(CompletableFuture.allOf(sender.closeAsync(), receiver.closeAsync())))
            .then(super.globalCleanupAsync());
    }
}
