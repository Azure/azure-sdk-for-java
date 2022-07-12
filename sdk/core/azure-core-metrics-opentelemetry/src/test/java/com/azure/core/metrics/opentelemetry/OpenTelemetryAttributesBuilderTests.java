// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenTelemetryAttributesBuilderTests {
    private static final MeterProvider METER_PROVIDER = new OpenTelemetryMeterProvider();
    private static final Meter METER = METER_PROVIDER.createMeter("test", null, null);

    @Test
    public void empty() {
        TelemetryAttributes attributeCollection = METER.createAttributes(Collections.emptyMap());
        assertEquals(OpenTelemetryAttributes.class, attributeCollection.getClass());
        Attributes attributes = ((OpenTelemetryAttributes) attributeCollection).get();
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void addAttribute() {
        TelemetryAttributes attributeCollection = METER.createAttributes(new HashMap<String, Object>() {{
                put("string", "string-value");
                put("long", 42L);
                put("boolean", true);
                put("double", 0.42d);
            }});

        assertEquals(OpenTelemetryAttributes.class, attributeCollection.getClass());
        Attributes attributes = ((OpenTelemetryAttributes) attributeCollection).get();

        assertEquals(4, attributes.size());
        assertEquals("string-value", attributes.get(AttributeKey.stringKey("string")));
        assertEquals(42L, attributes.get(AttributeKey.longKey("long")));
        assertTrue(attributes.get(AttributeKey.booleanKey("boolean")));
        assertEquals(0.42d, attributes.get(AttributeKey.doubleKey("double")));
    }

    @Test
    public void addAttributeInvalid() {
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap("string", null)));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap(null, "foo")));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap(null, 42L)));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap(null, false)));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap(null, 0.1d)));
    }
}
