// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OpenTelemetryTracerProviderTests {
    @Test
    public void noopTracer() {
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("foo", null, null, null);
        assertNotNull(tracer);
        assertFalse(tracer.isEnabled());
    }

    @Test
    public void invalidParams() {
        assertThrows(NullPointerException.class,
            () -> TracerProvider.getDefaultProvider().createTracer(null, null, null, null));
    }

    @Test
    public void getProviderReturnsOtelProvider() {
        assertSame(OpenTelemetryTracerProvider.class, new OpenTelemetryTracingOptions().getTracerProvider());
    }
}
