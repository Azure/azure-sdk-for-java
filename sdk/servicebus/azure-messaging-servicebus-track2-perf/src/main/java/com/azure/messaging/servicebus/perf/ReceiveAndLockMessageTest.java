// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;


import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Performance test.
 */
public class ReceiveAndLockMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private List<ServiceBusMessage> messages = new ArrayList<>();

    public ReceiveAndLockMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEK_LOCK);
    }

    private Mono<Void> sendMessages() {
        ServiceBusMessage message = new ServiceBusMessage(CONTENTS.getBytes());
        return senderAsync.sendMessage(message).then();
    }

    public Mono<Void> globalSetupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        int totalMessageMultiplier = 50;

        ServiceBusMessage message = new ServiceBusMessage(CONTENTS.getBytes());
        return Mono.defer(() -> {
            messages = IntStream.range(0, options.getMessagesToSend() * totalMessageMultiplier)
                .mapToObj(index -> message)
                .collect(Collectors.toList());
            return senderAsync.sendMessages(messages);
        });
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(options.getMessagesToReceive());
        for (ServiceBusReceivedMessageContext messageContext : messages) {
            receiver.complete(messageContext.getMessage().getLockToken());
        }
    }

    @Override
    public Mono<Void> runAsync() {
        Mono<Void> operator = receiverAsync
            .receiveMessages()
            .take(options.getMessagesToReceive())
            .map(messageContext -> {
                receiverAsync.complete(messageContext.getMessage().getLockToken()).block();
                return messageContext;
            })
            .then();
        return operator;
    }
}
