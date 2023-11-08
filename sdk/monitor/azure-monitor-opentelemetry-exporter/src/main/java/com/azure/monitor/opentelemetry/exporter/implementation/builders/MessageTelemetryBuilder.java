// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.SeverityLevel;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class MessageTelemetryBuilder extends AbstractTelemetryBuilder {

    private static final int MAX_MESSAGE_LENGTH = 32768;

    private final MessageData data;

    public static MessageTelemetryBuilder create() {
        return new MessageTelemetryBuilder(new MessageData());
    }

    private MessageTelemetryBuilder(MessageData data) {
        super(data, "Message", "MessageData");
        this.data = data;
    }

    public void setMessage(String message) {
        if (message.trim().isEmpty()) {
            // breeze doesn't accept message that is empty after trimming
            message = "n/a";
        }
        data.setMessage(truncateTelemetry(message, MAX_MESSAGE_LENGTH, "Message.message"));
    }

    public void setSeverityLevel(SeverityLevel severityLevel) {
        data.setSeverityLevel(severityLevel);
    }

    public void addMeasurement(@Nullable String key, Double value) {
        if (key == null || key.isEmpty() || key.length() > MAX_MEASUREMENT_KEY_LENGTH) {
            // TODO (trask) log
            return;
        }
        Map<String, Double> measurements = data.getMeasurements();
        if (measurements == null) {
            measurements = new HashMap<>();
            data.setMeasurements(measurements);
        }
        measurements.put(key, value);
    }

    @Override
    protected Map<String, String> getProperties() {
        Map<String, String> properties = data.getProperties();
        if (properties == null) {
            properties = new HashMap<>();
            data.setProperties(properties);
        }
        return properties;
    }
}
