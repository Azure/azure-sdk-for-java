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


/**
 * Performance test.
 */
public class ReceiveAndDeleteMessageTest extends ServiceTest<ServiceBusStressOptions> {

    public ReceiveAndDeleteMessageTest(ServiceBusStressOptions options) {
        super(options, ReceiveMode.RECEIVE_AND_DELETE);
    }


    public Mono<Void> globalSetupAsync() {
        // Since test does warm up and test many times, we are sending many messages, so we will have them available.
        int totalMessageMultiplier = 500;

        ServiceBusMessage message =  new ServiceBusMessage(CONTENTS.getBytes());
        return Flux.range(0, options.getMessagesToSend() * totalMessageMultiplier)
            .flatMap(count -> senderAsync.sendMessage(message))
            .then();
    }

    @Override
    public void run() {
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(options.getMessagesToReceive());
        for(ServiceBusReceivedMessageContext messageContext : messages) {
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
