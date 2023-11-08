// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.stress.util.RunResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getReceiverBuilder;

/**
 * Test ServiceBusReceiverAsyncClient
 */
@Component("MessageReceiverAsync")
public class MessageReceiverAsync extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageReceiverAsync.class);

    @Override
    public RunResult run() {
        AtomicReference<RunResult> result = new AtomicReference<>(RunResult.INCONCLUSIVE);
        ServiceBusReceiverAsyncClient client = toClose(getReceiverBuilder(options, false).buildAsyncClient());

        client.receiveMessages()
            .flatMap(message -> {
                LOGGER.verbose("message received: {}", message.getMessageId());
                return client.complete(message)
                    .onErrorResume(ex -> {
                        LOGGER.error("Completion error. messageId: {}, lockToken: {}",
                            message.getMessageId(),
                            message.getLockToken(),
                            ex);
                        result.set(RunResult.ERROR);
                        return Mono.empty();
                    });
            })
            .take(options.getTestDuration())
            .onErrorResume(error -> {
                result.set(RunResult.ERROR);
                LOGGER.error("error receiving", error);
                return Mono.empty();
            })
            .blockLast();

        return result.get();
    }
}
