// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.EventTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.ExceptionTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.MessageTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.RemoteDependencyTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.RequestTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryEventData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static com.azure.monitor.opentelemetry.autoconfigure.implementation.MappingsBuilder.MappingType.LOG;
import static com.azure.monitor.opentelemetry.autoconfigure.implementation.MappingsBuilder.MappingType.SPAN;

class MappingsTest {

    private static final AttributeKey<Value<?>> CUSTOM_MEASUREMENTS
        = AttributeKey.valueKey("microsoft.custom_measurements");

    @Test
    void mapsSpanCustomMeasurementsAndDropsInvalidEntries() {
        assertMapsValidCustomMeasurements(createMappings(SPAN),
            Arrays.asList(RequestTelemetryBuilder.create(), RemoteDependencyTelemetryBuilder.create(),
                MessageTelemetryBuilder.create(), ExceptionTelemetryBuilder.create()));
    }

    @Test
    void mapsLogCustomMeasurementsAndDropsInvalidEntries() {
        assertMapsValidCustomMeasurements(createMappings(LOG), Arrays.asList(MessageTelemetryBuilder.create(),
            EventTelemetryBuilder.create(), ExceptionTelemetryBuilder.create()));
    }

    @Test
    void doesNotMapMalformedSpanCustomMeasurementsToProperties() {
        assertDoesNotMapMalformedCustomMeasurements(createMappings(SPAN), RequestTelemetryBuilder.create());
    }

    @Test
    void doesNotMapMalformedLogCustomMeasurementsToProperties() {
        assertDoesNotMapMalformedCustomMeasurements(createMappings(LOG), EventTelemetryBuilder.create());
    }

    private static void assertMapsValidCustomMeasurements(Mappings mappings,
        Iterable<AbstractTelemetryBuilder> builders) {
        Map<String, Value<?>> values = new LinkedHashMap<>();
        values.put("itemsProcessed", Value.of(42.0));
        values.put("queueDepth", Value.of(7.0));
        values.put("notDouble", Value.of(1L));
        values.put("notFinite", Value.of(Double.NaN));
        values.put("positiveInfinity", Value.of(Double.POSITIVE_INFINITY));
        values.put("negativeInfinity", Value.of(Double.NEGATIVE_INFINITY));
        values.put("", Value.of(1.0));
        values.put(repeat('a', 151), Value.of(1.0));

        Attributes attributes
            = Attributes.builder().put(CUSTOM_MEASUREMENTS, Value.of(values)).put("color", "red").build();
        for (AbstractTelemetryBuilder builder : builders) {
            mappings.map(attributes, builder);

            MonitorDomain data = builder.build().getData().getBaseData();
            assertThat(getMeasurements(data)).containsOnly(entry("itemsProcessed", 42.0), entry("queueDepth", 7.0));
            assertThat(getProperties(data)).containsOnly(entry("color", "red"))
                .doesNotContainKey("microsoft.custom_measurements");
        }
    }

    private static void assertDoesNotMapMalformedCustomMeasurements(Mappings mappings,
        AbstractTelemetryBuilder builder) {
        Attributes attributes = Attributes.of(CUSTOM_MEASUREMENTS, Value.of("not a map"));
        mappings.map(attributes, builder);

        MonitorDomain data = builder.build().getData().getBaseData();
        assertThat(getMeasurements(data)).isNull();
        assertThat(getProperties(data)).isNull();
    }

    private static Mappings createMappings(MappingsBuilder.MappingType mappingType) {
        MappingsBuilder mappingsBuilder = new MappingsBuilder(mappingType);
        CustomMeasurementsMapper.register(mappingsBuilder);
        return mappingsBuilder.build();
    }

    private static Map<String, Double> getMeasurements(MonitorDomain data) {
        if (data instanceof RequestData) {
            return ((RequestData) data).getMeasurements();
        }
        if (data instanceof RemoteDependencyData) {
            return ((RemoteDependencyData) data).getMeasurements();
        }
        if (data instanceof MessageData) {
            return ((MessageData) data).getMeasurements();
        }
        if (data instanceof TelemetryExceptionData) {
            return ((TelemetryExceptionData) data).getMeasurements();
        }
        if (data instanceof TelemetryEventData) {
            return ((TelemetryEventData) data).getMeasurements();
        }
        throw new AssertionError("Unexpected telemetry data type: " + data.getClass().getName());
    }

    private static Map<String, String> getProperties(MonitorDomain data) {
        if (data instanceof RequestData) {
            return ((RequestData) data).getProperties();
        }
        if (data instanceof RemoteDependencyData) {
            return ((RemoteDependencyData) data).getProperties();
        }
        if (data instanceof MessageData) {
            return ((MessageData) data).getProperties();
        }
        if (data instanceof TelemetryExceptionData) {
            return ((TelemetryExceptionData) data).getProperties();
        }
        if (data instanceof TelemetryEventData) {
            return ((TelemetryEventData) data).getProperties();
        }
        throw new AssertionError("Unexpected telemetry data type: " + data.getClass().getName());
    }

    private static String repeat(char value, int count) {
        StringBuilder result = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            result.append(value);
        }
        return result.toString();
    }
}
