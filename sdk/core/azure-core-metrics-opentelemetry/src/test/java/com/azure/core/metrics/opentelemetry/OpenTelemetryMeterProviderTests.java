// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.test.utils.metrics.TestMeter;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OpenTelemetryMeterProviderTests {
    @Test
    public void noopMEter() {
        Meter meter = MeterProvider.getDefaultProvider().createMeter("foo", null, null);
        assertNotNull(meter);
        assertFalse(meter.isEnabled());
    }

    @Test
    public void invalidParams() {
        assertThrows(NullPointerException.class,
            () -> MeterProvider.getDefaultProvider().createMeter(null, null, null));
    }

    @Test
    public void getProviderReturnsOtelProvider() {
        assertSame(OpenTelemetryMeterProvider.class, new OpenTelemetryMetricsOptions().getMeterProvider());
    }

    private static class TestMeterProvider implements MeterProvider {
        @Override
        public Meter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
            return new TestMeter();
        }
    }
}
