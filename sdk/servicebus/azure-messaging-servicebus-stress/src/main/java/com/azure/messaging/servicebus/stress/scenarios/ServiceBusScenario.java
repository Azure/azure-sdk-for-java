// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.messaging.servicebus.stress.util.EntityType;
import com.azure.messaging.servicebus.stress.util.RunResult;
import com.azure.messaging.servicebus.stress.util.ScenarioOptions;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;

/**
 * Base class for service bus test scenarios
 */
public abstract class ServiceBusScenario implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusScenario.class);

    @Autowired
    protected ScenarioOptions options;

    protected ServiceBusAdministrationClient adminClient;
    protected ServiceBusReceiverClient receiverClient;

    private final List<AutoCloseable> toClose = new ArrayList<>();
    private Instant startTime;
    protected <T extends AutoCloseable> T toClose(T closeable) {
        toClose.add(closeable);
        return closeable;
    }

    protected Disposable toClose(Disposable closeable) {
        toClose.add(() -> closeable.dispose());
        return closeable;
    }

    /**
     * Run test scenario
     * @return test result
     */
    public abstract RunResult run() throws InterruptedException;

    public void beforeRun() {
        logTestConfiguration();
        adminClient = new ServiceBusAdministrationClientBuilder()
            .connectionString(options.getServiceBusConnectionString())
            .buildClient();

        receiverClient = options.getServiceBusEntityType() == EntityType.QUEUE ? new ServiceBusClientBuilder()
            .connectionString(options.getServiceBusConnectionString())
            .clientOptions(new ClientOptions().setTracingOptions(new TracingOptions().setEnabled(false)))
            .receiver()
            .disableAutoComplete()
            .queueName(options.getServiceBusQueueName())
            .buildClient() : null;

        startTime = Instant.now();
        blockingWait(options.getStartDelay());
    }

    public void afterRun(RunResult result) {
        LOGGER.atInfo()
            .addKeyValue("testClass", options.getTestClass())
            .addKeyValue("result", result.name())
            .addKeyValue("durationSec", (Instant.now().toEpochMilli() - startTime.toEpochMilli()) / 1000d)
            .log("test finished");

        logRemainingQueueMessages();
    }

    private void logTestConfiguration() {
        String serviceBusPackageVersion = "unknown";
        try {
            Class<?> serviceBusPackage = Class.forName("com.azure.messaging.servicebus.ServiceBusClientBuilder");
            serviceBusPackageVersion = serviceBusPackage.getPackage().getImplementationVersion();
            if (serviceBusPackageVersion == null) {
                serviceBusPackageVersion = "null";
            }
        } catch (ClassNotFoundException e) {
            LOGGER.warning("could not find ServiceBusClientBuilder class", e);
        }

        LOGGER.atInfo()
            .addKeyValue("duration", options.getTestDuration())
            .addKeyValue("tryTimeout", options.getTryTimeout())
            .addKeyValue("testClass", options.getTestClass())
            .addKeyValue("entityType", options.getServiceBusEntityType())
            .addKeyValue("queueName", options.getServiceBusQueueName())
            .addKeyValue("sessionQueueName", options.getServiceBusSessionQueueName())
            .addKeyValue("topicName", options.getServiceBusTopicName())
            .addKeyValue("serviceBusPackageVersion", serviceBusPackageVersion)
            .addKeyValue("subscriptionName", options.getServiceBusSubscriptionName())
            .addKeyValue("annotation", options.getAnnotation())
            .addKeyValue("connectionStringProvided", !CoreUtils.isNullOrEmpty(options.getServiceBusConnectionString()))
            .log("starting test");
    }
    @Override
    public void close() {
        if (toClose == null || toClose.size() == 0) {
            return;
        }

        for (final AutoCloseable closeable : toClose) {
            if (closeable == null) {
                continue;
            }

            try {
                closeable.close();
            } catch (Exception error) {
                LOGGER.error("[{}]: {} didn't close properly.", options.getTestClass(), closeable.getClass().getSimpleName(), error);
            }
        }

        toClose.clear();

        receiverClient.close();
    }

    protected int getRemainingQueueMessages() {
        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            QueueRuntimeProperties properties = adminClient.getQueueRuntimeProperties(options.getServiceBusQueueName());
            LOGGER.atInfo()
                .addKeyValue("activeCount", properties.getActiveMessageCount())
                .addKeyValue("scheduledCount", properties.getScheduledMessageCount())
                .addKeyValue("deadLetteredCount", properties.getDeadLetterMessageCount())
                .addKeyValue("transferredCount", properties.getTransferMessageCount())
                .addKeyValue("totalCount", properties.getTotalMessageCount())
                .log("Queue runtime properties");

            return properties.getActiveMessageCount();
        }

        return -1;
    }

    private void logRemainingQueueMessages() {
        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            int activeMessages = getRemainingQueueMessages();

            if (activeMessages > 0) {
                IterableStream<ServiceBusReceivedMessage> messages = receiverClient.peekMessages(activeMessages);
                messages.forEach(message -> LOGGER.atInfo()
                    .addKeyValue("messageId", message.getMessageId())
                    .addKeyValue("traceparent", message.getApplicationProperties().get("traceparent"))
                    .addKeyValue("deliveryCount", message.getDeliveryCount())
                    .addKeyValue("lockToken", message.getLockToken())
                    .addKeyValue("lockedUntil", message.getLockedUntil())
                    .log("active message"));
            }
        }
    }
}
