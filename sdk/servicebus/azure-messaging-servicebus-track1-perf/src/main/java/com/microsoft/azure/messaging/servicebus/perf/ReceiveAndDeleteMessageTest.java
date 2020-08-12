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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Performance test.
 */
public class ReceiveAndDeleteMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ClientLogger logger = new ClientLogger(ReceiveAndDeleteMessageTest.class);
    private final ServiceBusStressOptions options;

    private List<Message> messages = new ArrayList<>();

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
        super.globalCleanupAsync();
        try {
            sender.close();
        } catch (ServiceBusException e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
        return Mono.empty();
    }

    private Mono<Void> sendMessage() {
        //return Mono.fromFuture(sender.sendBatchAsync(messages));
        return Mono.fromRunnable(() -> {
            System.out.println("sendMessage Entry.. ");
            final int totalMessageMultiplier = 50;
            String messageId = UUID.randomUUID().toString();
            Message message = new Message(CONTENTS);
            message.setMessageId(messageId);
            messages = IntStream.range(0, options.getParallel() * options.getMessagesToSend() * totalMessageMultiplier)
                .mapToObj(index -> message)
                .collect(Collectors.toList());
            System.out.println("sendMessage Exit.. ");
        });
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        System.out.println("globalSetupAsync entry");
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        final int totalMessageMultiplier = 50;
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(CONTENTS);
        message.setMessageId(messageId);
        System.out.println("GlobalSetupAsync ..");
        return super.globalSetupAsync()
            .then(sendMessage());
            /*
            .map(aVoid -> {
                System.out.println("globalSetupAsync entry map.. ");
                messages = IntStream.range(0, options.getParallel() * options.getMessagesToSend() * totalMessageMultiplier)
                    .mapToObj(index -> message)
                    .collect(Collectors.toList());
                System.out.println("globalSetupAsync entry sending message with size : " + messages.size());
                return Mono.fromFuture(sender.sendBatchAsync(messages));
            }).then();

             */
    }

    @Override
    public void run() {
        System.out.println(" Sync received Entry");
        logger.verbose(" Sync received Entry ");
        Collection<IMessage> messages = null;
        try {
            messages = receiver.receiveBatch(options.getMessagesToReceive());
        } catch  (Exception e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
        int count = messages.size();
        logger.verbose(" Sync received count " + count);
        /*for (IMessage message : messages) {
            ++count;
        }*/
    }

    @Override
    public Mono<Void> runAsync() {
        System.out.println(" Async received Entry ");
        return Mono.fromFuture(receiver.receiveBatchAsync(options.getMessagesToReceive()))
            .then();
    }
}
