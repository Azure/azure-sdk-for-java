// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.*;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.BatchPerfTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ReceiveMessagesTest extends BatchPerfTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ReceiveMessagesTest.class);

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final ServiceBusSenderClient sender;
    private final ServiceBusReceiverAsyncClient receiverAsync;
    private final ServiceBusReceiverClient receiver;

    /**
     * Creates an instance of Batch performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ReceiveMessagesTest(ServiceBusStressOptions options) {
        super(options);
        String connectionString = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICE_BUS_CONNECTION_STRING)));
        }

        ServiceBusClientBuilder builder = new ServiceBusClientBuilder().connectionString(connectionString);
        String queueName = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(queueName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICEBUS_QUEUE_NAME)));
        }
        sender = builder.sender().queueName(queueName).buildClient();
        ServiceBusReceiveMode receiveMode = options.getIsDeleteMode() ? ServiceBusReceiveMode.RECEIVE_AND_DELETE : ServiceBusReceiveMode.PEEK_LOCK;
        receiver = builder.receiver().queueName(queueName).receiveMode(receiveMode).buildClient();
        receiverAsync = builder.receiver().queueName(queueName).receiveMode(receiveMode).buildAsyncClient();

    }

    @Override
    public int runBatch() {
        int count = 0;
        for (ServiceBusReceivedMessage message : receiver.receiveMessages(options.getMessagesToReceive())) {
            if (!options.getIsDeleteMode()) {
                receiver.complete(message);
                count++;
            }
        }
        if (count <= 0) {
            throw LOGGER.logExceptionAsWarning(new RuntimeException("Error. Should have received some messages."));
        }
        return count;
    }

    @Override
    public Mono<Integer> runBatchAsync() {
        AtomicInteger count = new AtomicInteger();
        return receiverAsync
            .receiveMessages()
            .take(options.getMessagesToReceive())
            .flatMap(message -> {
                count.getAndIncrement();
                return receiverAsync.complete(message);
            }, 1).then().thenReturn(count.get());

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
