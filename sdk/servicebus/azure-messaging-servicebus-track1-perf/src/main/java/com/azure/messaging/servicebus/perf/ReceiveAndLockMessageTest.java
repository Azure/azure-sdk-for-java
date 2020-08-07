// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Performance test.
 */
public class ReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private List<Message> messages = new ArrayList<>();

    public ReceiveAndLockMessageTest(ServiceBusStressOptions options) throws InterruptedException, ExecutionException, ServiceBusException {
        super(options, ReceiveMode.PEEKLOCK);
    }

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

            try{
                sender.sendBatch(messages);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ServiceBusException e) {
                e.printStackTrace();
            }
            return Mono.empty();
        });
    }

    @Override
    public void run() {
        Collection<IMessage> messages = null;
        try {
            messages = receiver.receiveBatch(options.getMessagesToReceive());
        } catch  (Exception ee) {
            ee.printStackTrace();
        }

        for(IMessage message : messages) {
            try {
                receiver.complete(message.getLockToken());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ServiceBusException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Mono<Void> runAsync() {
        CompletableFuture<Collection<IMessage>> receiveFuture = receiver.receiveBatchAsync(options.getMessagesToReceive());
        try {
            Collection<IMessage> messages = receiveFuture.get();
            for(IMessage message : messages){
                try {
                    receiver.complete(message.getLockToken());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ServiceBusException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return Mono.empty();
    }

    public Mono<Void> cleanupAsync() {
        try {
            sender.close();
        } catch (ServiceBusException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return Mono.empty();
    }
}
