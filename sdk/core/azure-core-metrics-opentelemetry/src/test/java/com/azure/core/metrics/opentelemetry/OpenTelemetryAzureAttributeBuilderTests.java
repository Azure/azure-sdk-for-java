package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AzureAttributeBuilder;
import com.azure.core.util.metrics.AzureMeterProvider;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenTelemetryAzureAttributeBuilderTests {
    private static final AzureMeterProvider METER_PROVIDER = new OpenTelemetryMeterProvider();

    @Test
    public void empty() {
        AzureAttributeBuilder attributeBuilder = METER_PROVIDER.createAttributeBuilder();
        assertEquals(OpenTelemetryAzureAttributeBuilder.class, attributeBuilder.getClass());
        Attributes attributes = ((OpenTelemetryAzureAttributeBuilder) attributeBuilder).build();
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void addAttribute() {
        AzureAttributeBuilder attributeBuilder = METER_PROVIDER.createAttributeBuilder();
        attributeBuilder.add("string", "string-value")
            .add("long", 42L)
            .add("boolean", true)
            .add("double", 0.42d);

        assertEquals(OpenTelemetryAzureAttributeBuilder.class, attributeBuilder.getClass());
        Attributes attributes = ((OpenTelemetryAzureAttributeBuilder) attributeBuilder).build();

        assertEquals(4, attributes.size());
        assertEquals("string-value", attributes.get(AttributeKey.stringKey("string")));
        assertEquals(42L, attributes.get(AttributeKey.longKey("long")));
        assertTrue(attributes.get(AttributeKey.booleanKey("boolean")));
        assertEquals(0.42d, attributes.get(AttributeKey.doubleKey("double")));
    }

    @Test
    public void addAttributeBuildMultiple() {
        AzureAttributeBuilder attributeBuilder = METER_PROVIDER.createAttributeBuilder();
        attributeBuilder.add("string", "string-value");
        Attributes attributes1 = ((OpenTelemetryAzureAttributeBuilder) attributeBuilder).build();
        assertEquals(1, attributes1.size());
        assertEquals("string-value", attributes1.get(AttributeKey.stringKey("string")));

        attributeBuilder.add("long", 42L);
        Attributes attributes2 = ((OpenTelemetryAzureAttributeBuilder) attributeBuilder).build();
        assertNotSame(attributes1, attributes2);

        assertEquals(1, attributes1.size());
        assertEquals(2, attributes2.size());
        assertEquals(42L, attributes2.get(AttributeKey.longKey("long")));

        assertSame(attributes2, ((OpenTelemetryAzureAttributeBuilder) attributeBuilder).build());
    }

    @Test
    public void addAttributeInvalid() {
        AzureAttributeBuilder attributeBuilder = METER_PROVIDER.createAttributeBuilder();
        assertThrows(NullPointerException.class, () -> attributeBuilder.add("string", null));
        assertThrows(NullPointerException.class, () -> attributeBuilder.add(null, "foo"));
        assertThrows(NullPointerException.class, () -> attributeBuilder.add(null, 42L));
        assertThrows(NullPointerException.class, () -> attributeBuilder.add(null, false));
        assertThrows(NullPointerException.class, () -> attributeBuilder.add(null, 0.1d));
    }
}
