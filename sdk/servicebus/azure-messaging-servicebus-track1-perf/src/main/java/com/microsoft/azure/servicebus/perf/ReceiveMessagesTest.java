// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.perf.core.ServiceBatchTest;
import com.microsoft.azure.servicebus.perf.core.ServiceBusStressOptions;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReceiveMessagesTest extends ServiceBatchTest<ServiceBusStressOptions> {
    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ReceiveMessagesTest(ServiceBusStressOptions options) {
        super(options);
    }

    @Override
    public int runBatch() {
        Collection<IMessage> messages;
        try {
            messages = receiver.receiveBatch(options.getMessagesToReceive());
            if (messages.size() <= 0) {
                throw new RuntimeException("Error. Should have received some messages.");
            }
        } catch (InterruptedException | ServiceBusException e) {
            throw new RuntimeException(e);
        }
        return messages.size();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        int receiveCount = options.getMessagesToReceive();
        return Mono.fromFuture(receiver.receiveBatchAsync(receiveCount))
            .handle((messages, synchronousSink) -> {
                int count = messages.size();
                if (count <= 0) {
                    synchronousSink.error(new RuntimeException("Error. Should have received some messages."));
                }
                synchronousSink.complete();
            }).then().thenReturn(receiveCount);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(sendMessage());
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromFuture(CompletableFuture.allOf(sender.closeAsync(), receiver.closeAsync()))
            .then(super.cleanupAsync());
    }

    private Mono<Void> sendMessage() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        for (int i = 0; i < TOTAL_MESSAGE_MULTIPLIER; i++) {
            List<IMessage> messages = ServiceBusTestUtil.getMessagesToSend(options.getMessagesSizeBytesToSend(), options.getMessagesToSend());
            sender.sendBatchAsync(messages);
        }
        return Mono.empty();
    }
}
