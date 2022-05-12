// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AttributesBuilder;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NoopMeterTests {
    @Test
    public void noopMeter() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertNotNull(noopMeter);
    }

    @Test
    public void noopMeterCreateInstrument() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertNotNull(noopMeter.createLongHistogram("name", "description", null));
        assertNotNull(noopMeter.createLongCounter("name", "description", null));
        assertNotNull(noopMeter.createLongUpDownCounter("name", "description", null));
    }

    @Test
    public void noopHistogramMeasurement() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);

        LongHistogram histogram = noopMeter.createLongHistogram("name", "description", null);
        assertFalse(histogram.isEnabled());
        AttributesBuilder attributes = noopMeter.createAttributesBuilder();
        assertNotNull(attributes);

        attributes.add("foo", "bar")
                .add("bar", true);
        histogram.record(42L, attributes, Context.NONE);
    }

    @Test
    public void noopCounterMeasurement() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        LongCounter counter = noopMeter.createLongCounter("name", "description", null);
        AttributesBuilder attributes = noopMeter.createAttributesBuilder()
            .add("foo", 42L)
            .add("bar", 0.42d);
        counter.add(42L, attributes, Context.NONE);
    }

    @Test
    public void noopUpDownCounterMeasurement() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        LongCounter counter = noopMeter.createLongUpDownCounter("name", "description", null);
        AttributesBuilder attributes = noopMeter.createAttributesBuilder()
            .add("foo", 42L)
            .add("bar", 0.42d);
        counter.add(42L, attributes, Context.NONE);
    }

    @Test
    public void createMeterNullNameThrows() {
        assertThrows(NullPointerException.class, () -> MeterProvider.getDefaultProvider().createMeter(null, null, null));
    }

    @Test
    public void noopMeterCreateInstrumentInvalidArgumentsThrow() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertThrows(NullPointerException.class, () -> noopMeter.createLongHistogram(null, "description", null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongHistogram("name", null, null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongCounter(null, "description", null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongCounter("name", null, null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongUpDownCounter(null, "description", null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongUpDownCounter("name", null, null));
    }
}
