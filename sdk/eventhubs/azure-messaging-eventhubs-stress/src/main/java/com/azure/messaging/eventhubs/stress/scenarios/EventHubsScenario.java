// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.stress.util.ScenarioOptions;
import com.azure.messaging.eventhubs.stress.util.TelemetryHelper;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for event hubs test scenarios
 */
public abstract class EventHubsScenario implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(EventHubsScenario.class);
    @Autowired
    protected ScenarioOptions options;

    protected final TelemetryHelper telemetryHelper = new TelemetryHelper(this.getClass());

    private final List<AutoCloseable> toClose = new ArrayList<>();

    public void beforeRun() {
    }

    public abstract void run();

    public void afterRun() {
    }

    protected <T extends AutoCloseable> T toClose(T closeable) {
        toClose.add(closeable);
        return closeable;
    }

    protected void toClose(Disposable closeable) {
        toClose.add(() -> closeable.dispose());
    }

    /**
     * Gets the builder with common configuration values set.
     *
     * @return The builder with common configuration set.
     */
    protected EventHubClientBuilder createEventHubClientBuilder() {
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(options.getEventHubsConnectionString());

        if (options.useV2Stack()) {
            Configuration configuration = new ConfigurationBuilder()
                .putProperty("com.azure.messaging.eventhubs.v2", "true")
                .build();

            builder.configuration(configuration);
        }

        if (options.getAmqpTransportType() != null) {
            builder.transportType(options.getAmqpTransportType());
        }

        return builder;
    }

    @Override
    public synchronized void close() {
        if (toClose.isEmpty()) {
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
    }

    public void recordRunOptions(Span span) {
    }

    public void recordResults(Span span) {
    }
}
