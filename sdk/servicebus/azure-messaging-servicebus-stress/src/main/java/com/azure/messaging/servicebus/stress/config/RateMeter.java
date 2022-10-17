// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.config;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.MetricTelemetry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Send metric telemetry periodically
 */
public class RateMeter {
    private static final ClientLogger LOGGER = new ClientLogger(RateMeter.class);

    private final Map<String, AtomicInteger> rateMap = new ConcurrentHashMap<>();
    private final TelemetryClient telemetryClient;
    private final Duration periodicDuration;
    private final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(2);

    /**
     * Constructor to create RateMeter
     *
     * @param telemetryClient The client to send metric telemetry
     * @param periodicDuration The sending period
     */
    public RateMeter(TelemetryClient telemetryClient, Duration periodicDuration) {
        this.telemetryClient = telemetryClient;
        this.periodicDuration = periodicDuration;
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

    private void runMetrics() {
        executorService.scheduleAtFixedRate(() -> {
            final List<MetricTelemetry> metricTelemetryList = new ArrayList<>();
            rateMap.forEach((key, count) -> {
                MetricTelemetry metricTelemetry = new MetricTelemetry();
                metricTelemetry.setValue(count.getAndSet(0));
                metricTelemetry.setTimestamp(new Date());
                metricTelemetry.setName(key);
                metricTelemetryList.add(metricTelemetry);
            });
            metricTelemetryList.forEach(metricTelemetry -> {
                LOGGER.info("Metrics: {Duration: {}, Metric: {}}", periodicDuration, metricTelemetry);
                telemetryClient.trackMetric(metricTelemetry);
            });
        }, periodicDuration.toMillis(), periodicDuration.toMillis(), TimeUnit.MILLISECONDS);
    }
}
