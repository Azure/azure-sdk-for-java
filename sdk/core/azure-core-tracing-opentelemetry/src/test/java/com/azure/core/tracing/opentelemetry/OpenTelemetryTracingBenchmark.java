// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 2, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class OpenTelemetryTracingBenchmark {
    private static final HttpPipeline NO_TRACING_PIPELINE = createPipeline(configureTracer(false), false);
    private static final HttpPipeline DISABLED_TRACING_PIPELINE = createPipeline(configureTracer(false), true);
    private static final HttpPipeline PIPELINE = createPipeline(configureTracer(true), true);

    @Benchmark
    public void noHttpTracing() {
        NO_TRACING_PIPELINE.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost/hello"),
            com.azure.core.util.Context.NONE);
    }

    @Benchmark
    public void disabledHttpTracing() {
        DISABLED_TRACING_PIPELINE.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost/hello"),
            com.azure.core.util.Context.NONE);
    }

    @Benchmark
    public void httpTracing() {
        PIPELINE.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost/hello"), com.azure.core.util.Context.NONE);
    }

    private static HttpPipeline createPipeline(Tracer tracer, boolean addTracingPolicy) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (addTracingPolicy) {
            HttpPolicyProviders.addAfterRetryPolicies(policies);
        }

        return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
            .tracer(tracer)
            .build();
    }

    private static Tracer configureTracer(boolean enabled) {
        if (!enabled) {
            return new OpenTelemetryTracer("benchmark", null, null,
                new OpenTelemetryTracingOptions().setEnabled(false));
        }

        SdkTracerProvider provider = SdkTracerProvider.builder()
            .setSampler(Sampler.traceIdRatioBased(0.01))
            .addSpanProcessor(new NoopProcessor())
            .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(provider).build();
        return new OpenTelemetryTracer("benchmark", null, null,
            new OpenTelemetryTracingOptions().setOpenTelemetry(openTelemetry));
    }

    static class NoopProcessor implements SpanProcessor {
        @Override
        public void onStart(Context context, ReadWriteSpan readWriteSpan) {
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
