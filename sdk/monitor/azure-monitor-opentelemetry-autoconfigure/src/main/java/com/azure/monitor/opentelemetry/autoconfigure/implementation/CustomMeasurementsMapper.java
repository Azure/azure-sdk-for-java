// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.CustomMeasurementsTelemetryBuilder;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.common.ValueType;

import java.util.List;

final class CustomMeasurementsMapper {

    private static final String ATTRIBUTE_NAME = "microsoft.custom_measurements";

    static void register(MappingsBuilder mappingsBuilder) {
        mappingsBuilder.exact(ATTRIBUTE_NAME, CustomMeasurementsMapper::map);
    }

    private static void map(AbstractTelemetryBuilder telemetryBuilder, Object value) {
        if (!(telemetryBuilder instanceof CustomMeasurementsTelemetryBuilder) || !(value instanceof Value)) {
            return;
        }
        Value<?> customMeasurements = (Value<?>) value;
        if (customMeasurements.getType() != ValueType.KEY_VALUE_LIST) {
            return;
        }
        CustomMeasurementsTelemetryBuilder measurementBuilder = (CustomMeasurementsTelemetryBuilder) telemetryBuilder;
        for (Object item : (List<?>) customMeasurements.getValue()) {
            if (!(item instanceof KeyValue)) {
                continue;
            }
            KeyValue entry = (KeyValue) item;
            Value<?> measurement = entry.getValue();
            if (measurement.getType() == ValueType.DOUBLE) {
                double doubleValue = (Double) measurement.getValue();
                if (Double.isFinite(doubleValue)) {
                    measurementBuilder.addMeasurement(entry.getKey(), doubleValue);
                }
            }
        }
    }

    private CustomMeasurementsMapper() {
    }
}
