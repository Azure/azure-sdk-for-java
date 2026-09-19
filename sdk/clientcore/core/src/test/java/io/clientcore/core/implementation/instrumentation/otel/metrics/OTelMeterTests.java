// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.instrumentation.otel.metrics;

import io.clientcore.core.implementation.instrumentation.NoopAttributes;
import io.clientcore.core.implementation.instrumentation.NoopMeter;
import io.clientcore.core.instrumentation.metrics.LongGauge;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for the OTelMeter class when OpenTelemetry isn't on the classpath.
 */
public class OTelMeterTests {
    @Test
    public void noopLongGaugeDoesNotInvokeCallback() throws Exception {
        AtomicInteger callbackCount = new AtomicInteger();
        LongGauge gauge = NoopMeter.INSTANCE.createLongGauge("core.test-gauge", "test gauge", "1");

        AutoCloseable registration = gauge.registerCallback(() -> {
            callbackCount.incrementAndGet();
            return 42L;
        }, NoopAttributes.INSTANCE);
        assertFalse(gauge.isEnabled());
        registration.close();

        assertEquals(0, callbackCount.get());
    }
}
