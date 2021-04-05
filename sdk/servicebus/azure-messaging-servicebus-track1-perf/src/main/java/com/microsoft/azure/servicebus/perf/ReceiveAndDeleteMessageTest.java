// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.perf;

import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.servicebus.perf.core.ServiceBusStressOptions;
import com.microsoft.azure.servicebus.perf.core.ServiceTest;
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
    private final ServiceBusStressOptions options;
    private final String messageContent;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ReceiveAndDeleteMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.RECEIVEANDDELETE);
        this.options = options;
        this.messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromFuture(CompletableFuture.allOf(sender.closeAsync(), receiver.closeAsync()))
            .then(super.cleanupAsync());
    }

    private Mono<Void> sendMessage() {
        int total =  options.getMessagesToSend() * TOTAL_MESSAGE_MULTIPLIER;

        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < total; ++i) {
            Message message = new Message(messageContent);
            message.setMessageId(UUID.randomUUID().toString());
            messages.add(message);
        }

        return Mono.fromFuture(sender.sendBatchAsync(messages));
    }

    @Override
    public Mono<Void> setupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        return super.setupAsync()
            .then(sendMessage());
    }

    @Override
    public void run() {
        Collection<IMessage> messages;
        try {
            messages = receiver.receiveBatch(options.getMessagesToReceive());
            if (messages.size() <= 0) {
                throw new RuntimeException("Error. Should have received some messages.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.fromFuture(receiver.receiveBatchAsync(options.getMessagesToReceive()))
            .handle((messages, synchronousSink) -> {
                int count = messages.size();
                if (count <= 0) {
                    synchronousSink.error(new RuntimeException("Error. Should have received some messages."));
                }
                synchronousSink.complete();
            }).then();
    }
}
