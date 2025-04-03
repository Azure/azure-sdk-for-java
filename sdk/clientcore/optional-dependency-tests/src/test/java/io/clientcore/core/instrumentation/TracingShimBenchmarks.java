// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
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

import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class TracingShimBenchmarks {

    private Tracer shimTracer;
    private Tracer shimTracerDisabled;
    private io.opentelemetry.api.trace.Tracer otelTracer;
    private io.opentelemetry.api.trace.Tracer otelTracerDisabled;
    private OpenTelemetry openTelemetry;
    private SdkInstrumentationOptions sdkInstrumentationOptions;

    private static final AttributeKey<String> STRING_ATTRIBUTE_KEY_1 = AttributeKey.stringKey("string1");
    private static final AttributeKey<String> STRING_ATTRIBUTE_KEY_2 = AttributeKey.stringKey("string2");
    private static final AttributeKey<String> ERROR_TYPE_ATTRIBUTE_KEY = AttributeKey.stringKey("error.type");
    private static final AttributeKey<Long> INT_ATTRIBUTE_KEY = AttributeKey.longKey("int");
    private static final AttributeKey<Long> LONG_ATTRIBUTE_KEY = AttributeKey.longKey("long");
    private static final AttributeKey<Double> DOUBLE_ATTRIBUTE_KEY = AttributeKey.doubleKey("double");
    private static final AttributeKey<Boolean> BOOLEAN_ATTRIBUTE_KEY = AttributeKey.booleanKey("boolean");

    @Setup
    public void setupOtel() {
        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(new NoopProcessor()).build())
            .build();

        sdkInstrumentationOptions = new SdkInstrumentationOptions("test").setSdkVersion("https://localhost:8080");

        otelTracer = openTelemetry.getTracer("test");
        otelTracerDisabled = TracerProvider.noop().get("test");

        shimTracer = Instrumentation
            .create(new InstrumentationOptions().setTelemetryProvider(openTelemetry), sdkInstrumentationOptions)
            .getTracer();
        shimTracerDisabled = Instrumentation
            .create(new InstrumentationOptions().setTelemetryProvider(OpenTelemetry.noop()), sdkInstrumentationOptions)
            .getTracer();
    }

    @Benchmark
    public void shimTracingDisabled(Blackhole blackhole) {
        blackhole.consume(testShimSpan(shimTracerDisabled));
    }

    @Benchmark
    public void directTracingDisabled(Blackhole blackhole) {
        blackhole.consume(testOTelSpan(otelTracerDisabled));
    }

    @Benchmark
    public void shimTracing(Blackhole blackhole) {
        blackhole.consume(testShimSpan(shimTracer));
    }

    @Benchmark
    public void directTracing(Blackhole blackhole) {
        blackhole.consume(testOTelSpan(otelTracer));
    }

    @SuppressWarnings("try")
    private Span testShimSpan(Tracer tracer) {
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

    @SuppressWarnings("try")
    private io.opentelemetry.api.trace.Span testOTelSpan(io.opentelemetry.api.trace.Tracer tracer) {
        io.opentelemetry.api.trace.Span span = tracer.spanBuilder("test")
            .setSpanKind(io.opentelemetry.api.trace.SpanKind.CLIENT)
            .setAttribute(STRING_ATTRIBUTE_KEY_1, "test")
            .startSpan();

        if (span.isRecording()) {
            span.setAttribute(STRING_ATTRIBUTE_KEY_2, "test");
            span.setAttribute(INT_ATTRIBUTE_KEY, 42);
            span.setAttribute(LONG_ATTRIBUTE_KEY, 42L);
            span.setAttribute(DOUBLE_ATTRIBUTE_KEY, 42.0);
            span.setAttribute(BOOLEAN_ATTRIBUTE_KEY, true);
        }

        try (io.opentelemetry.context.Scope scope = span.makeCurrent()) {
            span.setAttribute(ERROR_TYPE_ATTRIBUTE_KEY, "canceled");
            span.setStatus(StatusCode.ERROR);
        }
        span.end();

        return span;
    }

    static class NoopProcessor implements SpanProcessor {

        @Override
        public void onStart(io.opentelemetry.context.Context context, ReadWriteSpan readWriteSpan) {

        }

        @Override
        public boolean isStartRequired() {
            return false;
        }

        @Override
        public void onEnd(ReadableSpan readableSpan) {

        }

        @Override
        public boolean isEndRequired() {
            return false;
        }
    }
}
