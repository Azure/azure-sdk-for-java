// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import reactor.core.publisher.Mono;

/**
 * Performance test that sends a single message using both the async and synchronous message senders.
 */
public class SendMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ServiceBusMessage message;
    private final ServiceBusSenderClient sender;
    private final ServiceBusSenderAsyncClient senderAsync;

    /**
     * Instantiates an instance of the test class.
     *
     * @param options to set performance test options.
     */
    public SendMessageTest(ServiceBusStressOptions options) {
        super(options);

        final ClientLogger logger = new ClientLogger(SendMessageTest.class);
        final String queueName = getQueueName();

        logger.info("Sending 1 message to '{}'", queueName);

        message = getMessages(1).get(0);
        sender = getBuilder()
            .sender()
            .queueName(queueName)
            .buildClient();
        senderAsync = getBuilder()
            .sender()
            .queueName(queueName)
            .buildAsyncClient();
    }

    @Override
    public void run() {
        sender.sendMessage(message);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessage(message);
    }
}
