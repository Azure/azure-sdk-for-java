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
public class BatchSendTest extends ServiceBatchTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(BatchSendTest.class);

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public BatchSendTest(ServiceBusStressOptions options) {
        super(options);
    }

    @Override
    public int runBatch() {
        List<ServiceBusMessage> messages = geMessagesToSend();
        sender.sendMessages(messages);
        return messages.size();
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        List<ServiceBusMessage> messages = geMessagesToSend();
        return senderAsync.sendMessages(messages).thenReturn(messages.size());
    }

    private List<ServiceBusMessage> geMessagesToSend() {
        List<ServiceBusMessage> messages = new ArrayList<>();
        String messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());
        for (int i = 0; i < options.getMessagesToSend(); i++) {
            ServiceBusMessage message = new ServiceBusMessage(messageContent);
            message.setMessageId(UUID.randomUUID().toString());
            messages.add(message);
        }
        return messages;
    }

}
