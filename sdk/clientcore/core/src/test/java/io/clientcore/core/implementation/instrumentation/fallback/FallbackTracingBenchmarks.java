// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.InstrumentationTestUtils;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class FallbackTracingBenchmarks {

    private Tracer fallbackTracerEnabledWithLogs;
    private Tracer fallbackTracerEnabledNoLogs;
    private Tracer fallbackTracerDisabled;

    @Setup
    public void setupOtel() {
        LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions("test");
        fallbackTracerDisabled
            = Instrumentation.create(new InstrumentationOptions().setTracingEnabled(false), libraryOptions).getTracer();

        ClientLogger loggerDisabled
            = InstrumentationTestUtils.setupLogLevelAndGetLogger(ClientLogger.LogLevel.WARNING, new NoopStream());
        fallbackTracerEnabledNoLogs
            = Instrumentation.create(new InstrumentationOptions().setTelemetryProvider(loggerDisabled), libraryOptions)
                .getTracer();

        ClientLogger loggerEnabled
            = InstrumentationTestUtils.setupLogLevelAndGetLogger(ClientLogger.LogLevel.INFORMATIONAL, new NoopStream());
        fallbackTracerEnabledWithLogs
            = Instrumentation.create(new InstrumentationOptions().setTelemetryProvider(loggerEnabled), libraryOptions)
                .getTracer();
    }

    @Benchmark
    public void fallbackTracerDisabled(Blackhole blackhole) {
        blackhole.consume(testFallbackSpan(fallbackTracerDisabled));
    }

    @Benchmark
    public void fallbackTracerEnabledNoLogs(Blackhole blackhole) {
        blackhole.consume(testFallbackSpan(fallbackTracerEnabledNoLogs));
    }

    @Benchmark
    public void fallbackTracerEnabledWithLogs(Blackhole blackhole) {
        blackhole.consume(testFallbackSpan(fallbackTracerEnabledWithLogs));
    }

    @SuppressWarnings("try")
    private Span testFallbackSpan(Tracer tracer) {
        Span span = tracer.spanBuilder("test", SpanKind.CLIENT, null).setAttribute("string1", "test").startSpan();

        if (span.isRecording()) {
            span.setAttribute("string2", "test");
            span.setAttribute("int", 42);
            span.setAttribute("long", 42L);
            span.setAttribute("double", 42.0);
            span.setAttribute("boolean", true);
        }

        try (TracingScope scope = span.makeCurrent()) {
            span.setError("canceled");
        }
        span.end();

        return span;
    }

    static class NoopStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {

        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }
    }
}
