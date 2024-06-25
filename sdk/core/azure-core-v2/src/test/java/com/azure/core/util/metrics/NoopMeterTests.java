// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import io.clientcore.core.util.Context;
import com.azure.core.v2.util.TelemetryAttributes;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NoopMeterTests {
    @Test
    public void noopMeter() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertNotNull(noopMeter);
        assertFalse(noopMeter.isEnabled());
    }

    @Test
    public void noopMeterCreateInstrument() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertNotNull(noopMeter.createDoubleHistogram("name", "description", null));
        assertNotNull(noopMeter.createLongCounter("name", "description", null));
        assertNotNull(noopMeter.createLongUpDownCounter("name", "description", null));
    }

    @Test
    public void noopHistogramMeasurement() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);

        DoubleHistogram histogram = noopMeter.createDoubleHistogram("name", "description", null);
        assertFalse(histogram.isEnabled());
        TelemetryAttributes attributes = noopMeter.createAttributes(Collections.singletonMap("foo", 42L));
        assertNotNull(attributes);

        histogram.record(42L, attributes, Context.none());
    }

    @Test
    public void noopInvalidAttributes() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);

        assertThrows(NullPointerException.class, () -> noopMeter.createAttributes(null));
        assertThrows(NullPointerException.class,
            () -> noopMeter.createAttributes(Collections.singletonMap(null, "foo")));
        assertThrows(NullPointerException.class,
            () -> noopMeter.createAttributes(Collections.singletonMap("foo", null)));
    }

    @Test
    public void noopCounterMeasurement() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        LongCounter counter = noopMeter.createLongCounter("name", "description", null);
        assertFalse(counter.isEnabled());
        TelemetryAttributes attributes = noopMeter.createAttributes(Collections.singletonMap("foo", 0.42d));
        counter.add(42L, attributes, Context.none());
    }

    @Test
    public void noopUpDownCounterMeasurement() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        LongCounter counter = noopMeter.createLongUpDownCounter("name", "description", null);
        assertFalse(counter.isEnabled());
        TelemetryAttributes attributes = noopMeter.createAttributes(Collections.emptyMap());
        counter.add(42L, attributes, Context.none());
    }

    @Test
    public void createMeterNullNameThrows() {
        assertThrows(NullPointerException.class,
            () -> MeterProvider.getDefaultProvider().createMeter(null, null, null));
    }

    @Test
    public void noopMeterCreateInstrumentInvalidArgumentsThrow() {
        Meter noopMeter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertThrows(NullPointerException.class, () -> noopMeter.createDoubleHistogram(null, "description", null));
        assertThrows(NullPointerException.class, () -> noopMeter.createDoubleHistogram("name", null, null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongCounter(null, "description", null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongCounter("name", null, null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongUpDownCounter(null, "description", null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongUpDownCounter("name", null, null));
    }
}
