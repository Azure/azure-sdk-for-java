// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util.tracing;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggingTracerTests {
    @Test
    public void basicTracing() {
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("test", null, null,
            new LoggingTracerProvider.LoggingTracingOptions());

        Context spanCtx = tracer.start("test", Context.NONE);
        Object span = spanCtx.getData("span").get();
        assertInstanceOf(LoggingTracerProvider.LoggingSpan.class, span);
        tracer.end(null, null, spanCtx);
    }

    @Test
    public void injectContext() {
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("test", null, null,
            new LoggingTracerProvider.LoggingTracingOptions());

        Context spanCtx = tracer.start("test", Context.NONE);
        LoggingTracerProvider.LoggingSpan span = (LoggingTracerProvider.LoggingSpan) spanCtx.getData("span").get();

        AtomicBoolean injectCalled = new AtomicBoolean();
        tracer.injectContext((k, v) -> {
            assertFalse(injectCalled.getAndSet(true));
            assertEquals(k, "traceparent");
            assertEquals(v, String.format("00-%s-%s-01", span.getTraceId(), span.getSpanId()));
        }, spanCtx);

        assertTrue(injectCalled.get());
        tracer.end(null, null, spanCtx);
    }

    @Test
    public void extractContext() {
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("test", null, null,
            new LoggingTracerProvider.LoggingTracingOptions());

        AtomicBoolean extractCalled = new AtomicBoolean();
        Context spanCtx = tracer.extractContext(k -> {
            assertFalse(extractCalled.getAndSet(true));
            assertEquals(k, "traceparent");
            return "00-abcdef0123456789abcdef0123456789-abcdef0123456789-01";
        });

        LoggingTracerProvider.LoggingSpan span = (LoggingTracerProvider.LoggingSpan) spanCtx.getData("span").get();
        assertEquals("abcdef0123456789abcdef0123456789", span.getTraceId());
        tracer.end(null, null, spanCtx);
    }
}
