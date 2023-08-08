// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.stress.util.EntityType;
import com.azure.messaging.servicebus.stress.util.ScenarioOptions;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class TestUtils {
    private static final Random RANDOM = new Random();
    private static final AmqpRetryOptions RETRY_OPTIONS = new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(5));
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

    static ServiceBusClientBuilder.ServiceBusReceiverClientBuilder getReceiverBuilder(ScenarioOptions options, boolean session) {
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = getBuilder(options).receiver();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(session ? options.getServiceBusSessionQueueName() : options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
            builder.subscriptionName(session ? options.getServicBusSessionSubscriptionName() : options.getServiceBusSubscriptionName());
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

    static List<ServiceBusMessage> createBatch(byte[] messagePayload, int batchSize) {
        return IntStream.range(0, batchSize)
            .mapToObj(unused -> new ServiceBusMessage(messagePayload))
            .collect(Collectors.toList());
    }

    static byte[] createMessagePayload(int messageSize) {
        final byte[] messagePayload = new byte[messageSize];
        RANDOM.nextBytes(messagePayload);
        return messagePayload;
    }

    private static ServiceBusClientBuilder getBuilder(ScenarioOptions options) {
        return new ServiceBusClientBuilder()
            .retryOptions(RETRY_OPTIONS)
            .connectionString(options.getServiceBusConnectionString());
    }

    static void blockingWait(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private TestUtils() {
    }
}
