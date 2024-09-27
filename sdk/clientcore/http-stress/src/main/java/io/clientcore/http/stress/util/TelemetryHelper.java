// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.stress.util;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.http.stress.StressOptions;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Classes;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Cpu;
import io.opentelemetry.instrumentation.runtimemetrics.java8.GarbageCollector;
import io.opentelemetry.instrumentation.runtimemetrics.java8.MemoryPools;
import io.opentelemetry.instrumentation.runtimemetrics.java8.Threads;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static com.azure.perf.test.core.PerfStressOptions.HttpClientType.JDK;
import static com.azure.perf.test.core.PerfStressOptions.HttpClientType.OKHTTP;

/**
 * Telemetry helper is used to monitor test execution and record stats.
 */
public class TelemetryHelper {
    private final Tracer tracer;
    private final ClientLogger logger;
    private static final AttributeKey<String> ERROR_TYPE_ATTRIBUTE = AttributeKey.stringKey("error.type");
    private static final AttributeKey<Boolean> SAMPLE_IN_ATTRIBUTE = AttributeKey.booleanKey("sample.in");
    private static final AttributeKey<Long> DURATION_SEC_ATTRIBUTE = AttributeKey.longKey("durationSec");
    private static final AttributeKey<Long> DURATION_MS_ATTRIBUTE = AttributeKey.longKey("durationMs");
    private static final AttributeKey<String> SCENARIO_NAME_ATTRIBUTE = AttributeKey.stringKey("scenarioName");
    private static final AttributeKey<Long> CONCURRENCY_ATTRIBUTE = AttributeKey.longKey("concurrency");
    private static final AttributeKey<Boolean> SYNC_ATTRIBUTE = AttributeKey.booleanKey("sync");
    private static final AttributeKey<Long> SIZE_ATTRIBUTE = AttributeKey.longKey("size");
    private static final AttributeKey<String> HOSTNAME_ATTRIBUTE = AttributeKey.stringKey("hostname");
    private static final AttributeKey<String> SERVICE_ENDPOINT_ATTRIBUTE = AttributeKey.stringKey("serviceEndpoint");
    private static final AttributeKey<String> HTTP_CLIENT_ATTRIBUTE = AttributeKey.stringKey("httpClient");
    private static final AttributeKey<String> JRE_VERSION_ATTRIBUTE = AttributeKey.stringKey("jreVersion");
    private static final AttributeKey<String> JRE_VENDOR_ATTRIBUTE = AttributeKey.stringKey("jreVendor");
    private static final AttributeKey<String> GIT_COMMIT_ATTRIBUTE = AttributeKey.stringKey("gitCommit");
    private static final AttributeKey<Boolean> COMPLETEABLE_FUTURE_ATTRIBUTE = AttributeKey.booleanKey(
        "completeableFuture");
    private static final AttributeKey<Boolean> EXECUTOR_SERVICE_ATTRIBUTE = AttributeKey.booleanKey("executorService");
    private static final AttributeKey<Boolean> VIRTUAL_THREAD_ATTRIBUTE = AttributeKey.booleanKey("virtualThread");
    private final Attributes commonAttributes;
    private final Attributes canceledAttributes;

    private final String scenarioName;
    private final DoubleHistogram runDuration;

    static {
        // enables micrometer metrics from Reactor schedulers allowing to monitor thread pool usage and starvation
        Schedulers.enableMetrics();
    }

    /**
     * Creates a telemetry helper for a given scenario class.
     *
     * @param scenarioClass the scenario class
     */
    public TelemetryHelper(Class<?> scenarioClass) {
        this.scenarioName = scenarioClass.getName();
        this.commonAttributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName);
        this.canceledAttributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName, ERROR_TYPE_ATTRIBUTE,
            "cancelled");
        this.tracer = GlobalOpenTelemetry.getTracer(scenarioName);
        Meter meter = GlobalOpenTelemetry.getMeter(scenarioName);
        this.logger = new ClientLogger(scenarioName);
        this.runDuration = meter.histogramBuilder("test.run.duration").setUnit("s").build();
    }

    /**
     * Initializes telemetry helper: sets up Azure Monitor exporter, enables JVM metrics collection.
     */
    public static void init() {
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        String applicationInsightsConnectionString = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
        if (applicationInsightsConnectionString != null) {
            new AzureMonitorExporterBuilder().connectionString(applicationInsightsConnectionString).install(sdkBuilder);
        } else {
            System.setProperty("otel.traces.exporter", "none");
            System.setProperty("otel.logs.exporter", "none");
            System.setProperty("otel.metrics.exporter", "none");
        }

        OpenTelemetry otel = sdkBuilder
            // in case of multi-container test, customize instance id to distinguish telemetry from different containers
            //.addResourceCustomizer((resource, props) -> resource.toBuilder().put(AttributeKey.stringKey("service.instance.id"), "container-name-1").build())
            .addSamplerCustomizer((sampler, props) -> new Sampler() {
                @Override
                public SamplingResult shouldSample(Context parentContext, String traceId, String name,
                    SpanKind spanKind, Attributes attributes, List<LinkData> parentLinks) {
                    if (Boolean.TRUE.equals(attributes.get(SAMPLE_IN_ATTRIBUTE))) {
                        return SamplingResult.recordAndSample();
                    }
                    return sampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
                }

                @Override
                public String getDescription() {
                    return sampler.getDescription();
                }
            }).setResultAsGlobal().build().getOpenTelemetrySdk();
        Classes.registerObservers(otel);
        Cpu.registerObservers(otel);
        MemoryPools.registerObservers(otel);
        Threads.registerObservers(otel);
        GarbageCollector.registerObservers(otel);
        OpenTelemetryAppender.install(otel);
    }

    /**
     * Instruments a runnable: records runnable duration along with the status (success, error, cancellation),
     *
     * @param oneRun the runnable to instrument
     */
    @SuppressWarnings("try")
    public void instrumentRun(Runnable oneRun) {
        long start = System.currentTimeMillis();
        Span span = tracer.spanBuilder("run").startSpan();
        try (Scope s = span.makeCurrent()) {
            oneRun.run();
            trackSuccess(start, span);
        } catch (Throwable e) {
            if (e.getMessage().contains("Timeout on blocking read")
                || e instanceof InterruptedException
                || e instanceof TimeoutException) {
                trackCancellation(start, span);
            } else {
                trackFailure(start, e, span);
            }
        }
    }

    /**
     * Instruments a Mono: records mono duration along with the status (success, error, cancellation),
     *
     * @param runAsync the mono to instrument
     * @return the instrumented mono
     */
    @SuppressWarnings("try")
    public Mono<Void> instrumentRunAsync(Mono<Void> runAsync) {
        return Mono.defer(() -> {
            long start = System.currentTimeMillis();
            Span span = tracer.spanBuilder("runAsync").startSpan();
            try (Scope s = span.makeCurrent()) {
                return runAsync.doOnError(e -> trackFailure(start, e, span))
                    .doOnCancel(() -> trackCancellation(start, span))
                    .doOnSuccess(v -> trackSuccess(start, span))
                    .contextWrite(
                        reactor.util.context.Context.of(com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY,
                            io.opentelemetry.context.Context.current()));
            }
        });
    }

    /**
     * Instruments a CompletableFuture: records future duration along with the status (success, error, cancellation),
     *
     * @param runAsyncFuture the future to instrument
     * @return the instrumented future
     */
    @SuppressWarnings("try")
    public CompletableFuture<Void> instrumentRunAsyncWithCompletableFuture(CompletableFuture<Void> runAsyncFuture) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            Span span = tracer.spanBuilder("runAsyncCompletableFuture").startSpan();

            return new AbstractMap.SimpleImmutableEntry<>(start, span);
        }).thenCompose(startAndSpan -> {
            long start = startAndSpan.getKey();
            Span span = startAndSpan.getValue();

            return runAsyncFuture.whenComplete((result, throwable) -> {
                try (Scope s = span.makeCurrent()) {
                    if (throwable != null) {
                        trackFailure(start, throwable, span);
                    } else {
                        trackSuccess(start, span);
                    }
                } finally {
                    span.end();
                }
            });
        });
    }

    /**
     * Instruments a Runnable: records runnable duration along with the status (success, error, cancellation).
     *
     * @param task the runnable to instrument
     * @return A {@link Runnable} with instrumentation wrapping the {@code task}.
     */
    @SuppressWarnings("try")
    public Runnable instrumentRunAsyncWithRunnable(Runnable task) {
        return () -> {
            long start = System.currentTimeMillis();
            Span span = tracer.spanBuilder("runAsyncRunnable").startSpan();
            try (Scope s = span.makeCurrent()) {
                try {
                    task.run();
                    trackSuccess(start, span);
                } catch (Exception e) {
                    trackFailure(start, e, span);
                } finally {
                    span.end();
                }
            }
        };
    }

    private void trackSuccess(long start, Span span) {
        logger.atInfo().log("run ended");

        runDuration.record(getDuration(start), commonAttributes);
        span.end();
    }

    private void trackCancellation(long start, Span span) {
        logger.atWarning().addKeyValue("error.type", "cancelled").log("run ended");

        runDuration.record(getDuration(start), canceledAttributes);
        span.setAttribute(ERROR_TYPE_ATTRIBUTE, "cancelled");
        span.setStatus(StatusCode.ERROR);
        span.end();
    }

    private void trackFailure(long start, Throwable e, Span span) {
        Throwable unwrapped = Exceptions.unwrap(e);
        if (unwrapped instanceof UncheckedIOException) {
            unwrapped = unwrapped.getCause();
        }

        span.recordException(unwrapped);
        span.setStatus(StatusCode.ERROR, unwrapped.getMessage());

        String errorType = unwrapped.getClass().getName();
        logger.atError().addKeyValue("error.type", errorType).log("run ended", unwrapped);

        Attributes errorAttributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName, ERROR_TYPE_ATTRIBUTE,
            errorType);
        runDuration.record(getDuration(start), errorAttributes, io.opentelemetry.context.Context.current().with(span));
        span.end();
    }

    /**
     * Records an event representing the start of a test along with test options.
     *
     * @param options test parameters
     */
    public void recordStart(StressOptions options) {
        Span before = startSampledInSpan("before run");
        before.setAttribute(DURATION_SEC_ATTRIBUTE, options.getDuration());
        before.setAttribute(SCENARIO_NAME_ATTRIBUTE, scenarioName);
        before.setAttribute(CONCURRENCY_ATTRIBUTE, options.getParallel());

        before.setAttribute(SYNC_ATTRIBUTE, options.isSync());
        before.setAttribute(SIZE_ATTRIBUTE, options.getSize());
        before.setAttribute(HOSTNAME_ATTRIBUTE, System.getenv().get("HOSTNAME"));
        before.setAttribute(SERVICE_ENDPOINT_ATTRIBUTE, options.getServiceEndpoint());
        if (options.getHttpClient() == JDK) {
            before.setAttribute(HTTP_CLIENT_ATTRIBUTE, "jdk");
        } else if (options.getHttpClient() == OKHTTP) {
            before.setAttribute(HTTP_CLIENT_ATTRIBUTE, "okhttp");
        } else {
            before.setAttribute(HTTP_CLIENT_ATTRIBUTE, "default");
        }
        before.setAttribute(JRE_VERSION_ATTRIBUTE, System.getProperty("java.version"));
        before.setAttribute(JRE_VENDOR_ATTRIBUTE, System.getProperty("java.vendor"));
        before.setAttribute(GIT_COMMIT_ATTRIBUTE, System.getenv("GIT_COMMIT"));
        before.setAttribute(COMPLETEABLE_FUTURE_ATTRIBUTE, options.isCompletableFuture());
        before.setAttribute(EXECUTOR_SERVICE_ATTRIBUTE, options.isExecutorService());
        before.setAttribute(VIRTUAL_THREAD_ATTRIBUTE, options.isVirtualThread());

        before.end();
    }

    /**
     * Records an event representing the end of the test.
     *
     * @param startTime the start time of the test
     */
    public void recordEnd(long startTime) {
        Span after = startSampledInSpan("after run");
        after.setAttribute(DURATION_MS_ATTRIBUTE, System.currentTimeMillis() - startTime);
        after.end();
    }

    private Span startSampledInSpan(String name) {
        return tracer.spanBuilder(name)
            // guarantee that we have before/after spans sampled in
            // and record duration/result of the test
            .setAttribute(SAMPLE_IN_ATTRIBUTE, true).startSpan();
    }

    private static double getDuration(long start) {
        return Math.max(0L, System.currentTimeMillis() - start) / 1000.0D;
    }
}
