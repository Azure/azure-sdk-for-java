// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.messaging.servicebus.perf.core.ServiceBusStressOptions;
import com.microsoft.azure.messaging.servicebus.perf.core.ServiceTest;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Performance test.
 */
public class SendMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ClientLogger logger = new ClientLogger(SendMessageTest.class);
    private Message message  = null;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public SendMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.PEEKLOCK);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return Mono.defer(() -> {
            String messageId = UUID.randomUUID().toString();
            message = new Message(CONTENTS);
            message.setMessageId(messageId);
            return Mono.empty();
        });
    }

    @Override
    public void run() {
        try {

            sender.send(message);
        } catch (InterruptedException | ServiceBusException e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        try {
            sender.sendAsync(message).get();
        } catch (InterruptedException | ExecutionException e) {
            throw logger.logExceptionAsWarning(new RuntimeException(e));
        }
        return Mono.empty();
    }
}
