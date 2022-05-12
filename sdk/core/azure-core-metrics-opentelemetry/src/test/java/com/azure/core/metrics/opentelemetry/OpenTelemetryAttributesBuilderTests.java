// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributesBuilder;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenTelemetryAttributesBuilderTests {
    private static final MeterProvider METER_PROVIDER = new OpenTelemetryMeterProvider();
    private static final Meter METER = METER_PROVIDER.createMeter("test", null, null);

    @Test
    public void empty() {
        AttributesBuilder attributeCollection = METER.createAttributesBuilder();
        assertEquals(OpenTelemetryAttributesBuilder.class, attributeCollection.getClass());
        Attributes attributes = ((OpenTelemetryAttributesBuilder) attributeCollection).build();
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void addAttribute() {
        AttributesBuilder attributeCollection = METER.createAttributesBuilder();
        attributeCollection.add("string", "string-value")
            .add("long", 42L)
            .add("boolean", true)
            .add("double", 0.42d);

        assertEquals(OpenTelemetryAttributesBuilder.class, attributeCollection.getClass());
        Attributes attributes = ((OpenTelemetryAttributesBuilder) attributeCollection).build();

        assertEquals(4, attributes.size());
        assertEquals("string-value", attributes.get(AttributeKey.stringKey("string")));
        assertEquals(42L, attributes.get(AttributeKey.longKey("long")));
        assertTrue(attributes.get(AttributeKey.booleanKey("boolean")));
        assertEquals(0.42d, attributes.get(AttributeKey.doubleKey("double")));
    }

    @Test
    public void addAttributeBuildMultiple() {
        AttributesBuilder attributeCollection = METER.createAttributesBuilder();
        attributeCollection.add("string", "string-value");
        Attributes attributes1 = ((OpenTelemetryAttributesBuilder) attributeCollection).build();
        assertEquals(1, attributes1.size());
        assertEquals("string-value", attributes1.get(AttributeKey.stringKey("string")));

        attributeCollection.add("long", 42L);
        Attributes attributes2 = ((OpenTelemetryAttributesBuilder) attributeCollection).build();
        assertNotSame(attributes1, attributes2);

        assertEquals(1, attributes1.size());
        assertEquals(2, attributes2.size());
        assertEquals(42L, attributes2.get(AttributeKey.longKey("long")));

        assertSame(attributes2, ((OpenTelemetryAttributesBuilder) attributeCollection).build());
    }

    @Test
    public void addAttributeInvalid() {
        AttributesBuilder attributeCollection = METER.createAttributesBuilder();
        assertThrows(NullPointerException.class, () -> attributeCollection.add("string", null));
        assertThrows(NullPointerException.class, () -> attributeCollection.add(null, "foo"));
        assertThrows(NullPointerException.class, () -> attributeCollection.add(null, 42L));
        assertThrows(NullPointerException.class, () -> attributeCollection.add(null, false));
        assertThrows(NullPointerException.class, () -> attributeCollection.add(null, 0.1d));
    }
}
