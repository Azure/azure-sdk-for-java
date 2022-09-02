/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.TELEMETRY_TRUNCATION_ERROR;

final class TelemetryTruncation {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryTruncation.class);

    private static final Set<String> alreadyLoggedAttributeNames = ConcurrentHashMap.newKeySet();
    private static final Set<String> alreadyLoggedPropertyKeys = ConcurrentHashMap.newKeySet();

    private TelemetryTruncation() {
    }

    @SuppressWarnings("try")
    static String truncateTelemetry(@Nullable String value, int maxLength, String attributeName) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        if (alreadyLoggedAttributeNames.add(attributeName)) {
            // this can be expected, so don't want to flood the logs with a lot of these
            // (and don't want to log the full value, e.g. sql text > 8192 characters)
            try (MDC.MDCCloseable ignored = TELEMETRY_TRUNCATION_ERROR.makeActive()) {
                logger.warn(
                    "truncated {} attribute value to {} characters (this message will only be logged once"
                        + " per attribute name): {}",
                    attributeName,
                    maxLength,
                    trimTo80(value));
            }
        }
        logger.debug(
            "truncated {} attribute value to {} characters: {}", attributeName, maxLength, value);
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
                logger.warn(
                    "truncated {} property value to {} characters (this message will only be logged once"
                        + " per property key, and only for at most 10 different property keys): {}",
                    propertyKey,
                    maxLength,
                    trimTo80(value));
            }
        }
        logger.debug("truncated {} property value to {} characters: {}", propertyKey, maxLength, value);
        return value.substring(0, maxLength);
    }

    private static String trimTo80(String value) {
        if (value.length() <= 80) {
            return value;
        }
        return value.substring(0, 80) + "...";
    }
}
