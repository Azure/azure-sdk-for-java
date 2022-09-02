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

import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryEventData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class EventTelemetryBuilder extends AbstractTelemetryBuilder {

    private static final int MAX_EVENT_NAME_LENGTH = 512;

    private final TelemetryEventData data;

    private EventTelemetryBuilder(TelemetryEventData data) {
        super(data, "Event", "EventData");
        this.data = data;
    }

    public static EventTelemetryBuilder create() {
        return new EventTelemetryBuilder(new TelemetryEventData());
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
