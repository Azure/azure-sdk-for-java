// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.azure.messaging.servicebus.stress.util.TestUtils.getReceiverBuilder;

/**
 * Test ServiceBusReceiverAsyncClient
 */
@Component("MessageReceiverAsync")
public class MessageReceiverAsync extends ServiceBusScenario {
    @Override
    public void run() {
        ServiceBusReceiverAsyncClient client = toClose(getReceiverBuilder(options, false).buildAsyncClient());

        client.receiveMessages().flatMap(message -> client.complete(message).onErrorResume(ex -> {
            telemetryHelper.recordError(ex, "complete");
            return Mono.empty();
        })).take(options.getTestDuration()).onErrorResume(error -> {
            telemetryHelper.recordError(error, "receiveMessages");
            return Mono.empty();
        }).blockLast();
    }
}
