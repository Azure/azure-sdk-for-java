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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Performance test.
 */
public class SendMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    private List<IMessage> messages = new ArrayList<>();

    public SendMessagesTest(ServiceBusStressOptions options) throws InterruptedException, ExecutionException, ServiceBusException {
        super(options, ReceiveMode.PEEKLOCK);
    }

    public Mono<Void> globalSetupAsync() {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(CONTENTS);
        message.setMessageId(messageId);
        return Flux.range(0, options.getMessagesToSend())
            .flatMap(count -> {
                messages.add(message);
                return Mono.empty();
            })
            .then();
    }

    @Override
    public void run() {
        try {
            sender.sendBatch(messages);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ServiceBusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mono<Void> runAsync() {
        try {
            sender.sendBatchAsync(messages).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }
}
