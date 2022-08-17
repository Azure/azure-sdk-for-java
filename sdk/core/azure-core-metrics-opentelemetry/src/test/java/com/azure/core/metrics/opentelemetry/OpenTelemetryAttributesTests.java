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

public class OpenTelemetryAttributesTests {
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
                put("float", 4.2f);
                put("short", (short)1);
                put("byte", (byte)2);
                put("int", 3);
                put("unknown", this); // will be ignored
            }});

        assertEquals(OpenTelemetryAttributes.class, attributeCollection.getClass());
        Attributes attributes = ((OpenTelemetryAttributes) attributeCollection).get();

        assertEquals(8, attributes.size());
        assertEquals("string-value", attributes.get(AttributeKey.stringKey("string")));
        assertEquals(42L, attributes.get(AttributeKey.longKey("long")));
        assertTrue(attributes.get(AttributeKey.booleanKey("boolean")));
        assertEquals(0.42d, attributes.get(AttributeKey.doubleKey("double")));
        assertEquals(4.2d, attributes.get(AttributeKey.doubleKey("float")));
        assertEquals(1, attributes.get(AttributeKey.doubleKey("short")));
        assertEquals(2, attributes.get(AttributeKey.doubleKey("byte")));
        assertEquals(3, attributes.get(AttributeKey.doubleKey("int")));
    }

    @Test
    public void attributeMappings() {
        TelemetryAttributes attributeCollection = METER.createAttributes(new HashMap<String, Object>() {{
            put("foobar", "value");
            put("hostName", "host");
            put("entityName", "entity");
            put("entityPath", "path");
            put("amqpError", "amqp::error::code");
        }});

        assertEquals(OpenTelemetryAttributes.class, attributeCollection.getClass());
        Attributes attributes = ((OpenTelemetryAttributes) attributeCollection).get();

        assertEquals(5, attributes.size());
        assertEquals("value", attributes.get(AttributeKey.stringKey("foobar")));
        assertEquals("host", attributes.get(AttributeKey.stringKey("net.peer.name")));
        assertEquals("entity", attributes.get(AttributeKey.stringKey("messaging.destination")));
        assertEquals("path", attributes.get(AttributeKey.stringKey("messaging.az.destination.path")));
        assertEquals("amqp::error::code", attributes.get(AttributeKey.stringKey("amqp.error_code")));
    }

    @Test
    public void attributeLongMappings() {
        TelemetryAttributes attributeCollection = METER.createAttributes(Collections.singletonMap("amqpError", 42));

        assertEquals(OpenTelemetryAttributes.class, attributeCollection.getClass());
        Attributes attributes = ((OpenTelemetryAttributes) attributeCollection).get();

        assertEquals(1, attributes.size());
        assertEquals(42, attributes.get(AttributeKey.longKey("amqp.error_code")));
    }

    @Test
    public void addAttributeInvalid() {
        assertThrows(NullPointerException.class, () -> METER.createAttributes(null));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap("string", null)));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap(null, "foo")));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap(null, 42L)));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap(null, false)));
        assertThrows(NullPointerException.class, () -> METER.createAttributes(Collections.singletonMap(null, 0.1d)));
    }
}
