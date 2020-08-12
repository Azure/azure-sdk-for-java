// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;


import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.azure.messaging.servicebus.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        super(options, ReceiveMode.PEEK_LOCK);
        this.options = options;
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        return Mono.defer(() -> {
            ServiceBusMessage message = new ServiceBusMessage(CONTENTS.getBytes(Charset.defaultCharset()));
            int total = options.getParallel() * options.getMessagesToSend() * TOTAL_MESSAGE_MULTIPLIER;
            List<ServiceBusMessage> messages = IntStream.range(0, total)
                .mapToObj(index -> message)
                .collect(Collectors.toList());
            return senderAsync.sendMessages(messages);
        });
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver
            .receiveMessages(options.getMessagesToReceive());
        for (ServiceBusReceivedMessageContext messageContext : messages) {
            receiver.complete(messageContext.getMessage().getLockToken());
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return receiverAsync
            .receiveMessages()
            .take(options.getMessagesToReceive())
            .map(messageContext -> {
                receiverAsync.complete(messageContext.getMessage().getLockToken()).block();
                return messageContext;
            })
            .then();
    }
}
