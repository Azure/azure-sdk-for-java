// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.ServiceBusMessage;
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
public class SendMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    private List<ServiceBusMessage> messages = new ArrayList<>();

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public SendMessagesTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEK_LOCK);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes(Charset.defaultCharset()));
        return Mono.defer(() -> {
            messages = IntStream.range(0, options.getMessagesToSend())
                .mapToObj(index -> message)
                .collect(Collectors.toList());
            return Mono.empty();
        });
    }

    @Override
    public void run() {
        sender.sendMessages(messages);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessages(messages).then();
    }
}
