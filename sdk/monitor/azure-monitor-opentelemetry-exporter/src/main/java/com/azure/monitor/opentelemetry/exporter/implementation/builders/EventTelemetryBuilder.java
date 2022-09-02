// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryEventData;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class EventTelemetryBuilder extends AbstractTelemetryBuilder {

    private static final int MAX_EVENT_NAME_LENGTH = 512;

    private final TelemetryEventData data;

    public static EventTelemetryBuilder create() {
        return new EventTelemetryBuilder(new TelemetryEventData());
    }

    private EventTelemetryBuilder(TelemetryEventData data) {
        super(data, "Event", "EventData");
        this.data = data;
    }

    public void setName(String name) {
        data.setName(truncateTelemetry(name, MAX_EVENT_NAME_LENGTH, "Event.name"));
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
