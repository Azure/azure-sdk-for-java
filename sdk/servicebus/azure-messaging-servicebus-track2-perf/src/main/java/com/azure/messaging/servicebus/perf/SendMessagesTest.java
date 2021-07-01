// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Performance test.
 */
public class SendMessagesTest extends ServiceTest<ServiceBusStressOptions> {
    private final List<ServiceBusMessage> messages;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public SendMessagesTest(ServiceBusStressOptions options) {
        super(options, ServiceBusReceiveMode.PEEK_LOCK);
        String messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());
        messages = new ArrayList<>();
        for (int i = 0; i < options.getMessagesToSend(); ++i) {
            ServiceBusMessage message =  new ServiceBusMessage(messageContent);
            message.setMessageId(UUID.randomUUID().toString());
            messages.add(message);
        }

    }

    @Override
    public void run() {
        sender.sendMessages(messages);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessages(messages);
    }

}
