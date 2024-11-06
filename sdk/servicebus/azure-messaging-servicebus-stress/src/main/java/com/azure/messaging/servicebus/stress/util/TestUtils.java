// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.util;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class TestUtils {
    private static final byte[] PAYLOAD
        = "this is a circular payload that is used to fill up the message".getBytes(StandardCharsets.UTF_8);
    private static final Tracer TRACER = GlobalOpenTelemetry.getTracer("ServiceBusScenarioRunner");
    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    public static ServiceBusClientBuilder.ServiceBusSenderClientBuilder getSenderBuilder(ScenarioOptions options,
        boolean session) {
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder = getBuilder(options).sender();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(session ? options.getServiceBusSessionQueueName() : options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
        }

        return builder;
    }

    public static ServiceBusClientBuilder.ServiceBusReceiverClientBuilder getReceiverBuilder(ScenarioOptions options,
        boolean session) {
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder = getBuilder(options).receiver();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(session ? options.getServiceBusSessionQueueName() : options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
            builder.subscriptionName(
                session ? options.getServiceBusSessionSubscriptionName() : options.getServiceBusSubscriptionName());
        }

        return builder.receiveMode(ServiceBusReceiveMode.PEEK_LOCK).disableAutoComplete();
    }

    public static ServiceBusClientBuilder.ServiceBusProcessorClientBuilder
        getProcessorBuilder(ScenarioOptions options) {
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder = getBuilder(options).processor();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
            builder.subscriptionName(options.getServiceBusSubscriptionName());
        }

        return builder.receiveMode(ServiceBusReceiveMode.PEEK_LOCK).disableAutoComplete();
    }

    public static ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder
        getSessionProcessorBuilder(ScenarioOptions options) {
        ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder
            = getBuilder(options).sessionProcessor();

        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            builder.queueName(options.getServiceBusQueueName());
        } else if (options.getServiceBusEntityType() == EntityType.TOPIC) {
            builder.topicName(options.getServiceBusTopicName());
            builder.subscriptionName(options.getServiceBusSubscriptionName());
        }

        return builder.receiveMode(ServiceBusReceiveMode.PEEK_LOCK).disableAutoComplete();
    }

    public static ServiceBusMessageBatch createBatchSync(ServiceBusSenderClient client, BinaryData messagePayload,
        int batchSize) {
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

    public static BinaryData createMessagePayload(int messageSize) {
        final StringBuilder body = new StringBuilder(messageSize);
        for (int i = 0; i < messageSize; i++) {
            body.append(PAYLOAD[i % PAYLOAD.length]);
        }
        return BinaryData.fromString(body.toString());
    }

    private static ServiceBusClientBuilder getBuilder(ScenarioOptions options) {
        return new ServiceBusClientBuilder().retryOptions(new AmqpRetryOptions().setTryTimeout(options.getTryTimeout()))
            .connectionString(options.getServiceBusConnectionString());
    }

    public static boolean blockingWait(Duration duration) {
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
