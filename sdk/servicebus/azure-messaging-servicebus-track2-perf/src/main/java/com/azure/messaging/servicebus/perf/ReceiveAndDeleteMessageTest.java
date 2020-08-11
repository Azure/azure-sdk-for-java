// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Performance test.
 */
public class ReceiveAndDeleteMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private List<ServiceBusMessage> messages = new ArrayList<>();
    private final ServiceBusStressOptions options;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public ReceiveAndDeleteMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.RECEIVE_AND_DELETE);
        this.options = options;
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        int totalMessageMultiplier = 500;

        ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes(Charset.defaultCharset()));

        return Mono.defer(() -> {
            messages = IntStream.range(0, options.getMessagesToSend() * totalMessageMultiplier)
                .mapToObj(index -> message)
                .collect(Collectors.toList());
            return senderAsync.sendMessages(messages);
        });
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver
            .receiveMessages(options.getMessagesToReceive());
        int count = 0;
        for (ServiceBusReceivedMessageContext messageContext : messages) {
            ++count;
        }
    }

    @Override
    public Mono<Void> runAsync() {
        Mono<Void> operator = receiverAsync
            .receiveMessages()
            .take(options.getMessagesToReceive())
            .then();
        return operator;
    }
}
