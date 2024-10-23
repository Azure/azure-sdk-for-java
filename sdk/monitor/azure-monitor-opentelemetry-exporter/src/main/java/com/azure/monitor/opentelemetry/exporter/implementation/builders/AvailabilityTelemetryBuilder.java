// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.AvailabilityData;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class AvailabilityTelemetryBuilder extends AbstractTelemetryBuilder {

    private static final int MAX_RUN_LOCATION_LENGTH = 1024;
    private static final int MAX_MESSAGE_LENGTH = 8192;

    private final AvailabilityData data;

    public static AvailabilityTelemetryBuilder create() {
        return new AvailabilityTelemetryBuilder(new AvailabilityData());
    }

    private AvailabilityTelemetryBuilder(AvailabilityData data) {
        super(data, "Availability", "AvailabilityData");
        this.data = data;
    }

    public void setId(String id) {
        data.setId(truncateTelemetry(id, MAX_ID_LENGTH, "Availability.id"));
    }

    public void setName(String name) {
        data.setName(truncateTelemetry(name, MAX_NAME_LENGTH, "Availability.name"));
    }

    public void setDuration(String duration) {
        data.setDuration(duration);
    }

    public void setSuccess(boolean success) {
        data.setSuccess(success);
    }

    public void setRunLocation(String runLocation) {
        data.setRunLocation(truncateTelemetry(runLocation, MAX_RUN_LOCATION_LENGTH, "Availability.runLocation"));
    }

    public void setMessage(String message) {
        data.setMessage(truncateTelemetry(message, MAX_MESSAGE_LENGTH, "Availability.message"));
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
