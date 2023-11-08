// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.stress.util.EntityType;
import com.azure.messaging.servicebus.stress.util.ScenarioOptions;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

final class TestUtils {
    private static final byte[] PAYLOAD = "this is a circular payload that is used to fill up the message".getBytes(StandardCharsets.UTF_8);

    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    static ServiceBusClientBuilder.ServiceBusSenderClientBuilder getSenderBuilder(ScenarioOptions options, boolean session) {
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder = getBuilder(options)
            .sender();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(session ? options.getServiceBusSessionQueueName() : options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
        }

        return builder;
    }

    public static ServiceBusClientBuilder.ServiceBusReceiverClientBuilder getReceiverBuilder(ScenarioOptions options, boolean session) {
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = getBuilder(options).receiver();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(session ? options.getServiceBusSessionQueueName() : options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
            builder.subscriptionName(session ? options.getServiceBusSessionSubscriptionName() : options.getServiceBusSubscriptionName());
        }

        return builder
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete();
    }

    static ServiceBusClientBuilder.ServiceBusProcessorClientBuilder getProcessorBuilder(ScenarioOptions options) {
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = getBuilder(options).processor();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
            builder.subscriptionName(options.getServiceBusSubscriptionName());
        }

        return builder
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete();
    }

    static ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder getSessionProcessorBuilder(ScenarioOptions options) {
        ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder = getBuilder(options).sessionProcessor();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
            builder.subscriptionName(options.getServiceBusSubscriptionName());
        }

        return builder
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete();
    }

    static ServiceBusMessageBatch createBatchSync(ServiceBusSenderClient client, byte[] messagePayload, int batchSize) {
        try {
            ServiceBusMessageBatch batch = client.createMessageBatch();
            for (int i = 0; i < batchSize; i++) {
                batch.tryAddMessage(new ServiceBusMessage(messagePayload));
            }

            return batch;
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException("Error creating batch", e));
        }
    }

    static byte[] createMessagePayload(int messageSize) {
        final byte[] messagePayload = new byte[messageSize];
        for (int i = 0; i < messageSize; i++) {
            messagePayload[i] = PAYLOAD[i % PAYLOAD.length];
        }
        return messagePayload;
    }

    protected static ServiceBusClientBuilder getBuilder(ScenarioOptions options) {
        return new ServiceBusClientBuilder()
            .retryOptions(new AmqpRetryOptions().setTryTimeout(options.getTryTimeout()))
            .connectionString(options.getServiceBusConnectionString());
    }

    static boolean blockingWait(Duration duration) {
        if (duration.toMillis() <= 0) {
            return true;
        }

        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            LOGGER.warning("wait interrupted");
            return false;
        }

        return true;
    }

    private TestUtils() {
    }
}
