// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.tracing;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.instrumentation.DefaultLogger;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultInstrumentationTests {
    private static final LibraryInstrumentationOptions DEFAULT_LIB_OPTIONS
        = new LibraryInstrumentationOptions("test-library");
    private final AccessibleByteArrayOutputStream logCaptureStream;

    public DefaultInstrumentationTests() {
        logCaptureStream = new AccessibleByteArrayOutputStream();
    }

    @Test
    public void createTracer() {
        Tracer tracer = Instrumentation.create(null, DEFAULT_LIB_OPTIONS).getTracer();
        assertTrue(tracer.isEnabled());

        Span span = tracer.spanBuilder("test-span", SpanKind.INTERNAL, null).startSpan();

        assertFalse(span.isRecording());
    }

    @Test
    public void createTracerTracingDisabled() {
        InstrumentationOptions<?> options = new InstrumentationOptions<>().setTracingEnabled(false);

        Tracer tracer = Instrumentation.create(options, DEFAULT_LIB_OPTIONS).getTracer();
        assertFalse(tracer.isEnabled());

        // should not throw
        Span span = tracer.spanBuilder("test-span", SpanKind.INTERNAL, null).startSpan();

        assertNotNull(span);
        assertFalse(span.isRecording());
    }

    @Test
    public void createTracerBadArguments() {
        InstrumentationOptions<?> options = new InstrumentationOptions<>().setProvider("this is not a valid provider");

        assertThrows(NullPointerException.class, () -> Instrumentation.create(options, null).getTracer());
    }

    private ClientLogger setupLogLevelAndGetLogger(ClientLogger.LogLevel logLevelToSet,
        Map<String, Object> globalContext) {
        DefaultLogger logger
            = new DefaultLogger(ClientLogger.class.getName(), new PrintStream(logCaptureStream), logLevelToSet);

        return new ClientLogger(ClientLogger.class);
    }

}
