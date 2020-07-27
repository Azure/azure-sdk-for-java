/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TelemetrySender {
    private static final Logger log = LoggerFactory.getLogger(TelemetrySender.class);

    private static final int INSTRUMENTATION_KEY_LENGTH = 36;

    private final TelemetryRestClient telemetryRestClient;

    private final TelemetryCollector collector;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TelemetrySender(String instrumentKey, @NonNull TelemetryCollector collector) {
        this.telemetryRestClient = buildTelemetryRestClient(instrumentKey);
        this.collector = collector;
        this.scheduler.scheduleAtFixedRate(this::sendEvent, 0, 1, TimeUnit.HOURS);
    }

    private static TelemetryRestClient buildTelemetryRestClient(String instrumentationKey) {
        if (!isValid(instrumentationKey)) {
            log.warn("Telemetry instrumentationKey {} is invalid", instrumentationKey);
            throw new IllegalArgumentException("Telemetry instrumentationKey is invalid");
        }

        return new TelemetryRestClient(instrumentationKey);
    }

    private static boolean isValid(String instrumentationKey) {
        return StringUtils.hasText(instrumentationKey) && instrumentationKey.length() == INSTRUMENTATION_KEY_LENGTH;
    }

    private void sendEvent() {
        this.collector.getProperties().forEach((m) -> {
            log.info("Sending telemetry event with properties {}", m);

            telemetryRestClient.send(collector.getName(), m);
        });
    }
}
