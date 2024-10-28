// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.QueueRuntimeProperties;
import com.azure.messaging.servicebus.stress.util.EntityType;
import com.azure.messaging.servicebus.stress.util.ScenarioOptions;
import com.azure.messaging.servicebus.stress.util.TelemetryHelper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;

import static com.azure.messaging.servicebus.stress.util.TestUtils.blockingWait;

/**
 * Base class for service bus test scenarios
 */
public abstract class ServiceBusScenario implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusScenario.class);
    protected final TelemetryHelper telemetryHelper = new TelemetryHelper(this.getClass());

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
     */
    public abstract void run() throws InterruptedException;

    public void beforeRun() {
        adminClient
            = new ServiceBusAdministrationClientBuilder().connectionString(options.getServiceBusConnectionString())
                .buildClient();

        receiverClient = options.getServiceBusEntityType() == EntityType.QUEUE
            ? new ServiceBusClientBuilder().connectionString(options.getServiceBusConnectionString())
                .clientOptions(new ClientOptions().setTracingOptions(new TracingOptions().setEnabled(false)))
                .receiver()
                .disableAutoComplete()
                .queueName(options.getServiceBusQueueName())
                .buildClient()
            : null;

        blockingWait(options.getStartDelay());
    }

    public void afterRun() {
    }

    public void recordRunOptions(Span span) {

    }

    public void recordResults(Span span) {

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

    @SuppressWarnings("try")
    protected int getRemainingQueueMessages() {
        if (options.getServiceBusEntityType() == EntityType.QUEUE) {
            Span span = telemetryHelper.startSampledInSpan("getRemainingQueueMessages");
            try (Scope s = span.makeCurrent()) {
                QueueRuntimeProperties properties
                    = adminClient.getQueueRuntimeProperties(options.getServiceBusQueueName());

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
