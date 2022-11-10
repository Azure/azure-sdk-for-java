// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Test ServiceBus receiver client receive messages performance. After receive messages, return a count of messages to record.
 */
public class ReceiveMessagesTest extends ServiceBusBatchTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ReceiveMessagesTest.class);

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ReceiveMessagesTest(ServiceBusStressOptions options) {
        super(options);
    }

    @Override
    public int runBatch() {
        int count = 0;
        for (ServiceBusReceivedMessage message : receiver.receiveMessages(options.getMessagesToReceive())) {
            if (!options.getIsDeleteMode()) {
                receiver.complete(message);
            }
            count++;
        }
        if (count <= 0) {
            throw LOGGER.logExceptionAsWarning(new RuntimeException("Error. Should have received some messages."));
        }
        return count;
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        int receiveCount = options.getMessagesToReceive();
        return Mono.using(
            receiverClientBuilder::buildAsyncClient,
            receiverAsync -> {
                return receiverAsync.receiveMessages()
                    .take(receiveCount)
                    .flatMap(message -> {
                        if (!options.getIsDeleteMode()) {
                            receiverAsync.complete(message);
                        }
                        return Mono.empty();
                    }, 1)
                    .then()
                    .thenReturn(receiveCount);
            },
            ServiceBusReceiverAsyncClient::close,
            true
        );
    }

    @Override
    public Mono<Void> setupAsync() {
        String messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());
        for (int i = 0; i < options.getMessageBatchSendTimes(); i++) {
            ServiceBusMessageBatch batch = sender.createMessageBatch();
            for (int j = 0; j < options.getMessageBatchSize(); j++) {
                ServiceBusMessage message = new ServiceBusMessage(messageContent);
                message.setMessageId(UUID.randomUUID().toString());
                batch.tryAddMessage(message);
            }
            sender.sendMessages(batch);
        }
        return Mono.empty();
    }

}
