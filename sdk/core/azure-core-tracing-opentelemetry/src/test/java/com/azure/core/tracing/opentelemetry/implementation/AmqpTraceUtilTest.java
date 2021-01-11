// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry.implementation;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadableSpan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


public class AmqpTraceUtilTest {

    private Tracer tracer;
    private Span parentSpan;
    private Scope scope;

    @BeforeEach
    public void setUp() {
        // Get the global singleton Tracer object.
        tracer = OpenTelemetrySdk.builder().build().getTracer("TracerSdkTest");
        // Start user parent span.
        parentSpan = tracer.spanBuilder(PARENT_SPAN_KEY).startSpan();
        scope = parentSpan.makeCurrent();
    }

    @AfterEach
    public void tearDown() {
        // Clear out tracer and tracingContext objects
        scope.close();
        tracer = null;
        assertNull(tracer);
    }

    @Test
    public void parseUnknownStatusMessage() {
        // Act
        ReadableSpan span2 = (ReadableSpan) AmqpTraceUtil.parseStatusMessage(parentSpan, "", null);

        // Assert
        assertNotNull(span2.toSpanData());
        assertEquals(StatusCode.UNSET, span2.toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void parseSuccessStatusMessage() {
        // Act

        ReadableSpan span2 = (ReadableSpan) AmqpTraceUtil.parseStatusMessage(parentSpan, "success", null);

        // Assert
        assertNotNull(span2.toSpanData());
        assertEquals(StatusCode.OK, span2.toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void parseStatusMessageOnError() {
        // Act
        ReadableSpan span2 = (ReadableSpan) AmqpTraceUtil.parseStatusMessage(parentSpan, "", new Error("testError"));

        // Assert
        assertNotNull(span2.toSpanData());
        assertEquals(StatusCode.ERROR, span2.toSpanData().getStatus().getStatusCode());
    }
}
