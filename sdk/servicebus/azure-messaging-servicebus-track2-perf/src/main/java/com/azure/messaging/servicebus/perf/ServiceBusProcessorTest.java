// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.perf.test.core.EventPerfTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Test ServiceBus processor client receive messages performance. Use eventRaised() and errorRaised() to record messages
 * count and error count.
 */
public class ServiceBusProcessorTest extends EventPerfTest<ServiceBusStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusProcessorTest.class);

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final String AZURE_SERVICEBUS_QUEUE_NAME = "AZURE_SERVICEBUS_QUEUE_NAME";

    private final String CONNECTION_STRING;
    private final String QUEUE_NAME;

    private final ServiceBusProcessorClient processor;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     * @throws IllegalStateException if SSL context cannot be created.
     */
    public ServiceBusProcessorTest(ServiceBusStressOptions options) {
        super(options);

        CONNECTION_STRING = System.getenv(AZURE_SERVICE_BUS_CONNECTION_STRING);
        if (CoreUtils.isNullOrEmpty(CONNECTION_STRING)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICE_BUS_CONNECTION_STRING)));
        }

        QUEUE_NAME = System.getenv(AZURE_SERVICEBUS_QUEUE_NAME);
        if (CoreUtils.isNullOrEmpty(QUEUE_NAME)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                String.format("Environment variable %s must be set", AZURE_SERVICEBUS_QUEUE_NAME)));
        }

        processor = new ServiceBusClientBuilder()
            .connectionString(CONNECTION_STRING)
            .processor()
            .queueName(QUEUE_NAME)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .maxConcurrentCalls(options.getMaxConcurrentCalls())
            .processMessage(messageContext -> {
                eventRaised();
            })
            .processError(errorContext -> {
                errorRaised(errorContext.getException());
            })
            .buildProcessorClient();

    }

    @Override
    public Mono<Void> globalSetupAsync() {
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString(CONNECTION_STRING)
            .sender()
            .queueName(QUEUE_NAME)
            .buildClient();

        String messageContent = TestDataCreationHelper.generateRandomString(options.getMessagesSizeBytesToSend());

        for (int i = 0; i < options.getMessageBatchSendTimes(); i++) {
            ServiceBusMessageBatch batch = sender.createMessageBatch();
            for (int j = 0; j < options.getMessagesToSend(); j++) {
                ServiceBusMessage message = new ServiceBusMessage(messageContent);
                message.setMessageId(UUID.randomUUID().toString());
                batch.tryAddMessage(message);
            }
            sender.sendMessages(batch);
        }
        return Mono.empty();
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.defer(() -> {
            processor.start();
            return Mono.empty();
        }));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.defer(() -> {
            processor.stop();
            return Mono.empty();
        }).then(super.cleanupAsync());
    }
}
