// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.tracing.opentelemetry.implementation;

import com.azure.core.tracing.opentelemetry.OpenTelemetryTracer;
import com.azure.core.util.Context;
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


public class HttpTraceUtilTest {

    private OpenTelemetryTracer openTelemetryTracer;
    private Tracer tracer;
    private Context tracingContext;
    private Span parentSpan;
    private Scope scope;

    @BeforeEach
    public void setUp() {
        openTelemetryTracer = new OpenTelemetryTracer();
        // Get the global singleton Tracer object.
        tracer = OpenTelemetrySdk.builder().build().getTracer("TracerSdkTest");
        // Start user parent span.
        parentSpan = tracer.spanBuilder(PARENT_SPAN_KEY).startSpan();
        scope = parentSpan.makeCurrent();
        // Add parent span to tracingContext
        tracingContext = new Context(PARENT_SPAN_KEY, parentSpan);
    }

    @AfterEach
    public void tearDown() {
        // Clear out tracer and tracingContext objects
        scope.close();
        tracer = null;
        tracingContext = null;
        assertNull(tracer);
        assertNull(tracingContext);
    }

    @Test
    public void parseUnknownStatusCode() {
        // Act

        ReadableSpan span2 = (ReadableSpan) HttpTraceUtil.setSpanStatus(parentSpan, 1, null);

        // Assert
        assertNotNull(span2.toSpanData());
        assertEquals(StatusCode.UNSET, span2.toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void parseUnauthenticatedStatusCode() {
        //Arrange
        final String errorMessage = "unauthenticated test user";

        // Act
        ReadableSpan span2 = (ReadableSpan) HttpTraceUtil.setSpanStatus(parentSpan, 401, null);

        // Assert
        assertNotNull(span2.toSpanData());
        assertEquals(StatusCode.ERROR, span2.toSpanData().getStatus().getStatusCode());
    }

    @Test
    public void parseNullError() {
        // Act
        ReadableSpan span2 = (ReadableSpan) HttpTraceUtil.setSpanStatus(parentSpan, 504, null);

        // Assert
        assertNotNull(span2.toSpanData());
        assertEquals(StatusCode.ERROR, span2.toSpanData().getStatus().getStatusCode());
    }
}
