// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util.tracing;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.core.util.tracing.TracingLink;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggingTracerTests {
    @Test
    public void basicTracing() {
        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, null, new LoggingTracerProvider.LoggingTracingOptions());

        Context spanCtx = tracer.start("test", Context.NONE);
        Object span = spanCtx.getData("span").get();
        assertInstanceOf(LoggingTracerProvider.LoggingSpan.class, span);
        tracer.end(null, null, spanCtx);
    }

    @Test
    public void injectContext() {
        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, null, new LoggingTracerProvider.LoggingTracingOptions());

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
        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, null, new LoggingTracerProvider.LoggingTracingOptions());

        AtomicBoolean extractCalled = new AtomicBoolean();
        Context spanCtx = tracer.extractContext(k -> {
            assertFalse(extractCalled.getAndSet(true));
            assertEquals(k, "traceparent");
            return "00-abcdef0123456789abcdef0123456789-abcdef0123456789-01";
        });

        assertEquals("abcdef0123456789abcdef0123456789", spanCtx.getData("traceId").get());
        assertEquals("abcdef0123456789", spanCtx.getData("spanId").get());
        tracer.end(null, null, spanCtx);
    }

    @Test
    public void tracingWithLinks() {
        Tracer tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, null, new LoggingTracerProvider.LoggingTracingOptions());

        TracingLink link1 = new TracingLink(
            new Context("traceId", "00000000000000000000000000000001").addData("spanId", "0000000000000001"),
            Collections.singletonMap("foo", "bar"));
        TracingLink link2 = new TracingLink(
            new Context("traceId", "20000000000000000000000000000000").addData("spanId", "2000000000000000"));

        StartSpanOptions startOptions
            = new StartSpanOptions(SpanKind.CONSUMER).setStartTimestamp(Instant.ofEpochSecond(42))
                .addLink(link1)
                .addLink(link2);

        LoggingTracerProvider.LoggingSpan parent = new LoggingTracerProvider.LoggingSpan("parent", SpanKind.SERVER,
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbb");
        Context spanCtx = tracer.start("test", startOptions, new Context("span", parent));
        Object span = spanCtx.getData("span").get();
        assertInstanceOf(LoggingTracerProvider.LoggingSpan.class, span);
        tracer.end(null, null, spanCtx);
    }
}
