// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test ServiceBus sender client send messages performance. After send success, return a count of messages to record.
 */
public class SendBatchTest extends ServiceBusBatchTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(SendBatchTest.class);

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public SendBatchTest(ServiceBusStressOptions options) {
        super(options);
    }

    @Override
    public int runBatch() {
        List<ServiceBusMessage> messages = ServiceBusTestUtil.geMessagesToSend(options.getMessagesSizeBytesToSend(), options.getMessagesToSend());
        sender.sendMessages(messages);
        return messages.size();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        List<ServiceBusMessage> messages = ServiceBusTestUtil.geMessagesToSend(options.getMessagesSizeBytesToSend(), options.getMessagesToSend());
        return senderAsync.sendMessages(messages).thenReturn(messages.size());
    }

}
