// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.messaging.servicebus.stress.util.EntityType;
import com.azure.messaging.servicebus.stress.util.ScenarioOptions;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;

import static com.azure.messaging.servicebus.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.util.TestUtils.startSampledInSpan;

/**
 * Base class for service bus test scenarios
 */
public abstract class ServiceBusScenario implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusScenario.class);
    private static final Meter METER = GlobalOpenTelemetry.getMeter("ServiceBusScenario");
    private static final LongCounter ERROR_COUNTER = METER.counterBuilder("stress_test.errors").build();

    @Autowired
    protected ScenarioOptions options;

    protected ServiceBusAdministrationClient adminClient;
    protected ServiceBusReceiverClient receiverClient;

    private final List<AutoCloseable> toClose = new ArrayList<>();

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
    public abstract void run() throws InterruptedException;

    public void beforeRun() {
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

        blockingWait(options.getStartDelay());
    }

    public void afterRun() {
    }

    public void recordRunOptions(Span span) {
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

        span.setAttribute(AttributeKey.stringKey("duration"), options.getTestDuration().toString());
        span.setAttribute(AttributeKey.stringKey("tryTimeout"), options.getTryTimeout().toString());
        span.setAttribute(AttributeKey.stringKey("testClass"), options.getTestClass());
        span.setAttribute(AttributeKey.stringKey("entityType"), options.getServiceBusEntityType().toString());
        span.setAttribute(AttributeKey.stringKey("queueName"), options.getServiceBusQueueName());
        span.setAttribute(AttributeKey.stringKey("topicName"), options.getServiceBusTopicName());
        span.setAttribute(AttributeKey.stringKey("sessionQueueName"), options.getServiceBusSessionQueueName());
        span.setAttribute(AttributeKey.stringKey("subscriptionName"), options.getServiceBusSubscriptionName());
        span.setAttribute(AttributeKey.stringKey("serviceBusPackageVersion"), serviceBusPackageVersion);
        span.setAttribute(AttributeKey.stringKey("annotation"), options.getAnnotation());
        span.setAttribute(AttributeKey.longKey("messageSize"), options.getMessageSize());
        span.setAttribute(AttributeKey.stringKey("hostname"), System.getenv().get("HOSTNAME"));
    }

    @Override
    public synchronized void close() {
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
                LOGGER.atError()
                    .addKeyValue("testClass", options.getTestClass())
                    .addKeyValue("closeable", closeable.getClass().getSimpleName())
                    .log("Couldn't close closeable", error);
            }
        }

        toClose.clear();
        receiverClient.close();
    }

    public void recordError(String errorReason, Throwable ex, String method) {
        Attributes attributes = Attributes.builder()
            .put(AttributeKey.stringKey("error.type"), errorReason)
            .put(AttributeKey.stringKey("method"), method)
            .build();
        ERROR_COUNTER.add(1, attributes);
        LoggingEventBuilder log = LOGGER.atError()
            .addKeyValue("error.type", errorReason)
            .addKeyValue("method", method);
        if (ex != null) {
            log.log("test error", ex);
        } else {
            log.log("test error");
        }
    }

    @SuppressWarnings("try")
    protected int getRemainingQueueMessages() {
        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            Span span = startSampledInSpan("getRemainingQueueMessages");
            try (Scope s = span.makeCurrent()) {
                QueueRuntimeProperties properties = adminClient.getQueueRuntimeProperties(options.getServiceBusQueueName());

                span.setAttribute(AttributeKey.longKey("activeCount"), properties.getActiveMessageCount());
                span.setAttribute(AttributeKey.longKey("scheduledCount"), properties.getScheduledMessageCount());
                span.setAttribute(AttributeKey.longKey("deadLetteredCount"), properties.getDeadLetterMessageCount());
                span.setAttribute(AttributeKey.longKey("transferredCount"), properties.getTransferMessageCount());
                span.setAttribute(AttributeKey.longKey("totalCount"), properties.getTotalMessageCount());

                return properties.getActiveMessageCount();
            } finally {
                span.end();
            }
        }

        return -1;
    }
}
