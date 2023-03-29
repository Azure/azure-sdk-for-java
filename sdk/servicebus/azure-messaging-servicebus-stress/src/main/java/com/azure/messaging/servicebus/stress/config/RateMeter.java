// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.config;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.MetricTelemetry;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Send metric telemetry periodically
 */
public class RateMeter implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(RateMeter.class);
    private static final String METRIC_NAMESPACE = "java.servicebus.stress";

    private final Map<String, AtomicInteger> rateMap = new ConcurrentHashMap<>();
    private final TelemetryClient telemetryClient;
    private final Duration periodicDuration;
    private final ScheduledExecutorService executorService;

    /**
     * Constructor to create RateMeter
     *
     * @param telemetryClient The client to send metric telemetry
     * @param periodicDuration The sending period
     */
    public RateMeter(TelemetryClient telemetryClient, Duration periodicDuration) {
        this.telemetryClient = telemetryClient;
        this.periodicDuration = periodicDuration;

        // Set the thread pool as a daemon so that the spring application can be closed once the test is finished.
        this.executorService = new ScheduledThreadPoolExecutor(2, r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });

        this.runMetrics();
    }

    /**
     * Add count for specific key
     *
     * @param key The key to add count
     * @param count The count to be added
     */
    public void add(String key, int count) {
        rateMap.computeIfAbsent(key, name -> new AtomicInteger(0));
        rateMap.get(key).addAndGet(count);
    }

    /**
     * Stop track metrics and close RateMeter by shutting down the underlying thread pool.
     */
    @Override
    public void close() {
        executorService.shutdown();
    }

    private void runMetrics() {
        executorService.scheduleAtFixedRate(() -> {
            final List<MetricTelemetry> metricTelemetryList = new ArrayList<>();
            rateMap.forEach((key, count) -> {
                MetricTelemetry metricTelemetry = new MetricTelemetry();
                metricTelemetry.setMetricNamespace(METRIC_NAMESPACE);
                metricTelemetry.setName(key);
                metricTelemetry.setValue(count.getAndSet(0));
                metricTelemetry.setTimestamp(Date.from(Instant.now()));
                metricTelemetryList.add(metricTelemetry);
            });
            metricTelemetryList.forEach(metricTelemetry -> {
                LOGGER.verbose("Metrics: {Duration: {}, Metric: {}}", periodicDuration, metricTelemetry);
                telemetryClient.trackMetric(metricTelemetry);
            });
        }, periodicDuration.toMillis(), periodicDuration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
