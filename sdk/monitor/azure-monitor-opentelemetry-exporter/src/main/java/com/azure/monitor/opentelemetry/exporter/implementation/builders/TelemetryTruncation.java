// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.core.util.logging.ClientLogger;
import org.slf4j.MDC;
import reactor.util.annotation.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.TELEMETRY_TRUNCATION_ERROR;

final class TelemetryTruncation {

    private static final ClientLogger logger = new ClientLogger(TelemetryTruncation.class);

    private static final Set<String> alreadyLoggedAttributeNames = ConcurrentHashMap.newKeySet();
    private static final Set<String> alreadyLoggedPropertyKeys = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("try")
    static String truncateTelemetry(@Nullable String value, int maxLength, String attributeName) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        if (alreadyLoggedAttributeNames.add(attributeName)) {
            // this can be expected, so don't want to flood the logs with a lot of these
            // (and don't want to log the full value, e.g. sql text > 8192 characters)
            try (MDC.MDCCloseable ignored = TELEMETRY_TRUNCATION_ERROR.makeActive()) {
                logger.warning("truncated {} attribute value to {} characters (this message will only be logged once"
                    + " per attribute name): {}", attributeName, maxLength, trimTo80(value));
            }
        }
        logger.verbose("truncated {} attribute value to {} characters: {}", attributeName, maxLength, value);
        return value.substring(0, maxLength);
    }

    @SuppressWarnings("try")
    static String truncatePropertyValue(@Nullable String value, int maxLength, String propertyKey) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        if (alreadyLoggedPropertyKeys.size() < 10 && alreadyLoggedPropertyKeys.add(propertyKey)) {
            // this can be expected, so don't want to flood the logs with a lot of these
            try (MDC.MDCCloseable ignored = TELEMETRY_TRUNCATION_ERROR.makeActive()) {
                logger.warning(
                    "truncated {} property value to {} characters (this message will only be logged once"
                        + " per property key, and only for at most 10 different property keys): {}",
                    propertyKey, maxLength, trimTo80(value));
            }
        }
        logger.verbose("truncated {} property value to {} characters: {}", propertyKey, maxLength, value);
        return value.substring(0, maxLength);
    }

    private static String trimTo80(String value) {
        if (value.length() <= 80) {
            return value;
        }
        return value.substring(0, 80) + "...";
    }

    private TelemetryTruncation() {
    }
}
