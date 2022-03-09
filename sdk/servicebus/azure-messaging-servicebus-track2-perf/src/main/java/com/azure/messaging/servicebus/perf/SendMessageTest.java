// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Performance test.
 */
public class SendMessageTest extends ServiceTest<ServiceBusStressOptions> {
    private final ServiceBusMessage message;

    /**
     * Creates test object
     * @param options to set performance test options.
     */
    public SendMessageTest(ServiceBusStressOptions options) {
        super(options, ServiceBusReceiveMode.PEEK_LOCK);
        String messageId = UUID.randomUUID().toString();
        message = new ServiceBusMessage(TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend()));
        message.setMessageId(messageId);
    }

    @Override
    public void run() {
        sender.sendMessage(message);
    }

    @Override
    public Mono<Void> runAsync() {
        return senderAsync.sendMessage(message).then();
    }
}
