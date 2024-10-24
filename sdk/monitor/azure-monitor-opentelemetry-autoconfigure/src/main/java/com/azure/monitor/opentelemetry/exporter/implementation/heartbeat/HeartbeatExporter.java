// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.heartbeat;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ThreadPoolUtils;
import io.opentelemetry.sdk.resources.Resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Concrete implementation of Heartbeat functionality.
 */
public class HeartbeatExporter {

    private static final ClientLogger logger = new ClientLogger(HeartbeatExporter.class);

    /**
     * The name of the heartbeat metric.
     */
    private static final String HEARTBEAT_SYNTHETIC_METRIC_NAME = "HeartbeatState";

    /**
     * Map to hold heartbeat properties.
     */
    private final ConcurrentMap<String, HeartBeatPropertyPayload> heartbeatProperties;

    /**
     * Telemetry item exporter used to send heartbeat.
     */
    private final Consumer<List<TelemetryItem>> telemetryItemsConsumer;

    /**
     * Telemetry builder consumer used to populate defaults properties.
     */
    private final BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer;

    /**
     * ThreadPool used for adding properties to concurrent dictionary.
     */
    private final ExecutorService propertyUpdateService;

    /**
     * Threadpool used to send data heartbeat telemetry.
     */
    private final ScheduledExecutorService heartBeatSenderService;

    public static void start(long intervalSeconds, BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer,
        Consumer<List<TelemetryItem>> telemetryItemsConsumer) {
        new HeartbeatExporter(intervalSeconds, telemetryInitializer, telemetryItemsConsumer);
    }

    public HeartbeatExporter(long intervalSeconds, BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer,
        Consumer<List<TelemetryItem>> telemetryItemsConsumer) {
        this.heartbeatProperties = new ConcurrentHashMap<>();
        this.propertyUpdateService = Executors.newCachedThreadPool(
            ThreadPoolUtils.createDaemonThreadFactory(HeartbeatExporter.class, "propertyUpdateService"));
        this.heartBeatSenderService = Executors.newSingleThreadScheduledExecutor(
            ThreadPoolUtils.createDaemonThreadFactory(HeartbeatExporter.class, "heartBeatSenderService"));

        this.telemetryItemsConsumer = telemetryItemsConsumer;
        this.telemetryInitializer = telemetryInitializer;

        // Submit task to set properties to dictionary using separate thread. we do not wait for the
        // results to come out as some I/O bound properties may take time.
        propertyUpdateService.execute(HeartbeatDefaultPayload.populateDefaultPayload(this));

        heartBeatSenderService.scheduleAtFixedRate(this::send, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public boolean addHeartBeatProperty(String propertyName, String propertyValue, boolean isHealthy) {

        if (heartbeatProperties.containsKey(propertyName)) {
            logger.verbose(
                "heartbeat property {} cannot be added twice. Please use setHeartBeatProperty instead to modify the value",
                propertyName);
            return false;
        }

        HeartBeatPropertyPayload payload = new HeartBeatPropertyPayload();
        payload.setHealthy(isHealthy);
        payload.setPayloadValue(propertyValue);
        heartbeatProperties.put(propertyName, payload);
        return true;
    }

    /**
     * Send the heartbeat item synchronously to application insights backend.
     */
    private void send() {
        try {
            telemetryItemsConsumer.accept(Collections.singletonList(gatherData()));
        } catch (RuntimeException e) {
            logger.warning("Error occured while sending heartbeat");
        }
    }

    /**
     * Creates and returns the heartbeat telemetry.
     *
     * @return Metric Telemetry which represent heartbeat.
     */
    // visible for testing
    TelemetryItem gatherData() {
        Map<String, String> properties = new HashMap<>();
        double numHealthy = 0;
        for (Map.Entry<String, HeartBeatPropertyPayload> entry : heartbeatProperties.entrySet()) {
            HeartBeatPropertyPayload payload = entry.getValue();
            properties.put(entry.getKey(), payload.getPayloadValue());
            numHealthy += payload.isHealthy() ? 0 : 1;
        }
        MetricTelemetryBuilder telemetryBuilder
            = MetricTelemetryBuilder.create(HEARTBEAT_SYNTHETIC_METRIC_NAME, numHealthy);
        // TODO (heya) this is an interesting problem how this should work
        // we might want to think of the Heartbeat as "library instrumentation"
        // and inject an "OpenTelemetry" instance into it, similar to how other
        // "library instrumentation" works, and then emit real OpenTelemetry metrics from it which
        // would then go through the normal metric exporter
        // (this is not a problem we need to solve in this PR)
        telemetryInitializer.accept(telemetryBuilder, Resource.empty());
        telemetryBuilder.addTag(ContextTagKeys.AI_OPERATION_SYNTHETIC_SOURCE.toString(),
            HEARTBEAT_SYNTHETIC_METRIC_NAME);

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            telemetryBuilder.addProperty(entry.getKey(), entry.getValue());
        }

        return telemetryBuilder.build();
    }
}
