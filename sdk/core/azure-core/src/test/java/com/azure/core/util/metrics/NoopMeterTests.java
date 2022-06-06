// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NoopMeterTests {
    @Test
    public void noopMeter() {
        AzureMeter noopMeter = AzureMeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertNotNull(noopMeter);
        assertFalse(noopMeter.isEnabled());
    }

    @Test
    public void noopMeterCreateInstrument() {
        AzureMeter noopMeter = AzureMeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertNotNull(noopMeter.createLongHistogram("name", "description", null));
        assertNotNull(noopMeter.createLongCounter("name", "description", null));
    }

    @Test
    public void createMeterNullNameThrows() {
        assertThrows(NullPointerException.class, () -> AzureMeterProvider.getDefaultProvider().createMeter(null, null, null));
    }

    @Test
    public void noopMeterCreateInstrumentInvalidArgumentsThrow() {
        AzureMeter noopMeter = AzureMeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertThrows(NullPointerException.class, () -> noopMeter.createLongHistogram(null, "description", null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongHistogram("name", null, null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongCounter(null, "description", null));
        assertThrows(NullPointerException.class, () -> noopMeter.createLongCounter("name", null, null));
    }
}
