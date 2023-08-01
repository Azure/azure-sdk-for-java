// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getReceiverBuilder;

/**
 * Test ServiceBusReceiverAsyncClient
 */
@Component("MessageReceiverAsync")
public class MessageReceiverAsync extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageReceiverAsync.class);
    @Value("${DURATION_IN_MINUTES:15}")
    private int durationInMinutes;

    @Override
    public void run() {
        ServiceBusReceiverAsyncClient client = getReceiverBuilder(options, false).buildAsyncClient();

        client.receiveMessages()
            .flatMap(message -> {
                LOGGER.verbose("message received: {}", message.getMessageId());
                return client.complete(message)
                    .onErrorResume(ex -> {
                        LOGGER.error("Completion error. messageId: {}, lockToken: {}",
                            message.getMessageId(),
                            message.getLockToken(),
                            ex);
                        return Mono.empty();
                    });
            })
            .take(durationInMinutes)
            .onErrorResume(error -> {
                LOGGER.error("error receiving", error);
                return Mono.empty();
            })
            .blockLast();
    }
}
