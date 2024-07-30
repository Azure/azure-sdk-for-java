// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
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

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Telemetry helper is used to monitor test execution and record stats.
 */
public class TelemetryHelper {
    private final Tracer tracer;
    private final ClientLogger logger;
    private static final AttributeKey<String> SCENARIO_NAME_ATTRIBUTE = AttributeKey.stringKey("scenario_name");
    private static final AttributeKey<String> ERROR_TYPE_ATTRIBUTE = AttributeKey.stringKey("error.type");
    private static final AttributeKey<Boolean> SAMPLE_IN_ATTRIBUTE = AttributeKey.booleanKey("sample.in");
    private static final OpenTelemetry OTEL;
    private final String scenarioName;
    private final Meter meter;
    private final DoubleHistogram runDuration;
    private final Attributes commonAttributes;
    private final Attributes canceledAttributes;
    private final String packageType;

    private final AtomicLong successfulRuns = new AtomicLong();
    private final AtomicLong failedRuns = new AtomicLong();

    static {
        // enables micrometer metrics from Reactor schedulers allowing to monitor thread pool usage and starvation
        Schedulers.enableMetrics();
        OTEL = init();
    }

    /**
     * Creates an instance of telemetry helper.
     * @param scenarioClass the scenario class
     */
    public TelemetryHelper(Class<?> scenarioClass) {
        this.scenarioName = scenarioClass.getName();
        this.tracer = OTEL.getTracer(scenarioName);
        this.meter = OTEL.getMeter(scenarioName);
        this.logger = new ClientLogger(scenarioName);
        this.runDuration = meter.histogramBuilder("test.run.duration")
            .setUnit("s")
            .build();
        this.commonAttributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName);
        this.canceledAttributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName, ERROR_TYPE_ATTRIBUTE, "cancelled");
        this.packageType = scenarioClass.getPackage().toString();
    }

    /**
     * Initializes telemetry helper: sets up Azure Monitor exporter, enables JVM metrics collection.
     */
    public static OpenTelemetry init() {
        if (OTEL != null) {
            return OTEL;
        }

        System.setProperty("otel.java.global-autoconfigure.enabled", "true");

        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        String applicationInsightsConnectionString = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
        if (applicationInsightsConnectionString == null) {
            System.setProperty("otel.traces.exporter", "none");
            System.setProperty("otel.metrics.exporter", "none");
            System.setProperty("otel.logs.exporter", "none");
        } else {
            new AzureMonitorExporterBuilder()
                .connectionString(applicationInsightsConnectionString)
                .install(sdkBuilder);
        }

        OpenTelemetry otel = sdkBuilder
            .addSamplerCustomizer((sampler, props) -> new Sampler() {
                @Override
                public SamplingResult shouldSample(Context parentContext, String traceId, String name, SpanKind spanKind, Attributes attributes, List<LinkData> parentLinks) {
                    if (Boolean.TRUE.equals(attributes.get(SAMPLE_IN_ATTRIBUTE))) {
                        return SamplingResult.recordAndSample();
                    }
                    return sampler.shouldSample(parentContext, traceId, name, spanKind, attributes, parentLinks);
                }

                @Override
                public String getDescription() {
                    return sampler.getDescription();
                }
            })
            .setResultAsGlobal()
            .build()
            .getOpenTelemetrySdk();
        Classes.registerObservers(otel);
        Cpu.registerObservers(otel);
        MemoryPools.registerObservers(otel);
        Threads.registerObservers(otel);
        GarbageCollector.registerObservers(otel);
        OpenTelemetryAppender.install(otel);
        return otel;
    }

    /**
     * Instruments a runnable: records runnable duration along with the status (success, error, cancellation),
     * @param oneRun the runnable to instrument
     */
    @SuppressWarnings("try")
    public void instrumentRun(ThrowingFunction oneRun) {
        Instant start = Instant.now();
        Span span = tracer.spanBuilder("run").startSpan();
        try (Scope s = span.makeCurrent()) {
            com.azure.core.util.Context ctx = new com.azure.core.util.Context(com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current());
            oneRun.run(ctx);
            trackSuccess(start, span);
        } catch (Throwable e) {
            if (e.getMessage().contains("Timeout on blocking read") || e instanceof InterruptedException || e instanceof TimeoutException) {
                trackCancellation(start, span);
            } else {
                trackFailure(start, e, span);
            }
        }
    }

    /**
     * Instruments a Mono: records mono duration along with the status (success, error, cancellation),
     * @param runAsync the mono to instrument
     * @return the instrumented mono
     */
    @SuppressWarnings("try")
    public Mono<Void> instrumentRunAsync(Function<com.azure.core.util.Context, Mono<Void>> runAsync) {
        return Mono.defer(() -> {
            Instant start = Instant.now();
            Span span = tracer.spanBuilder("runAsync").startSpan();
            try (Scope s = span.makeCurrent()) {
                com.azure.core.util.Context ctx = new com.azure.core.util.Context(com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current());
                return runAsync.apply(ctx).doOnError(e -> trackFailure(start, e, span))
                    .doOnCancel(() -> trackCancellation(start, span))
                    .doOnSuccess(v -> trackSuccess(start, span))
                    .contextWrite(reactor.util.context.Context.of(com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current()));
            }
        });
    }

    private void trackSuccess(Instant start, Span span) {
        logger.atVerbose()
            .addKeyValue("traceId", span.getSpanContext().getTraceId())
            .addKeyValue("status", "success")
            .log("run ended");

        runDuration.record(getDuration(start), commonAttributes);
        successfulRuns.incrementAndGet();
        span.end();
        logger.info("track success");
    }

    private void trackCancellation(Instant start, Span span) {
        logger.atWarning()
            .addKeyValue("error.type", "cancelled")
            .log("run ended");

        runDuration.record(getDuration(start), canceledAttributes);
        span.setAttribute(ERROR_TYPE_ATTRIBUTE, "cancelled");
        span.setStatus(StatusCode.ERROR);
        span.end();
    }

    private void trackFailure(Instant start, Throwable e, Span span) {
        Throwable unwrapped = Exceptions.unwrap(e);

        span.recordException(unwrapped);
        span.setAttribute(ERROR_TYPE_ATTRIBUTE, unwrapped.getClass().getName());
        span.setStatus(StatusCode.ERROR, unwrapped.getMessage());

        String errorType = unwrapped.getClass().getName();
        logger.atError()
            .addKeyValue("error.type", errorType)
            // due to sampling, most of the logs are available in console/file share'
            // without trace context.
            // The only way to correlate them is to explicitly log traceId/
            .addKeyValue("traceId", span.getSpanContext().getTraceId())
            .log("run ended", unwrapped);

        Attributes attributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName, ERROR_TYPE_ATTRIBUTE, errorType);
        runDuration.record(getDuration(start), attributes);
        failedRuns.incrementAndGet();
        logger.info("track failure");
        span.end();
    }

    /**
     * Records an event representing the start of a test along with test options.
     * @param options test parameters
     */
    public void recordStart(StorageStressOptions options) {
        Span before = startSampledInSpan("before run");
        before.setAttribute(AttributeKey.longKey("durationSec"), options.getDuration());
        before.setAttribute(AttributeKey.stringKey("scenarioName"), scenarioName);
        before.setAttribute(AttributeKey.longKey("concurrency"), options.getParallel());
        before.setAttribute(AttributeKey.stringKey("storagePackageVersion"), this.packageType);
        before.setAttribute(AttributeKey.booleanKey("sync"), options.isSync());
        before.setAttribute(AttributeKey.longKey("payloadSize"), options.getSize());
        before.setAttribute(AttributeKey.stringKey("hostname"), System.getenv().get("HOSTNAME"));
        before.setAttribute(AttributeKey.booleanKey("faultInjectionForDownloads"), options.isFaultInjectionEnabledForDownloads());
        before.setAttribute(AttributeKey.booleanKey("faultInjectionForUploads"), options.isFaultInjectionEnabledForUploads());
        before.setAttribute(AttributeKey.stringKey("httpClientProvider"), options.getHttpClient().toString());
        before.setAttribute(AttributeKey.stringKey("jreVersion"), System.getProperty("java.version"));
        before.setAttribute(AttributeKey.stringKey("jreVendor"), System.getProperty("java.vendor"));
        before.end();

        // be  sure to remove logging afterwards
        logger.atInfo()
            .addKeyValue("duration", options.getDuration())
            .addKeyValue("payloadSize", options.getSize())
            .addKeyValue("concurrency", options.getParallel())
            .addKeyValue("faultInjectionForDownloads", options.isFaultInjectionEnabledForDownloads())
            .addKeyValue("faultInjectionForUploads", options.isFaultInjectionEnabledForUploads())
            .addKeyValue("storagePackageVersion", this.packageType)
            .addKeyValue("sync", options.isSync())
            .addKeyValue("scenarioName", scenarioName)
            .log("starting test");
        logger.log(LogLevel.INFORMATIONAL, () -> "starting test");
    }

    /**
     * Records an event representing the end of the test.
     * @param startTime the start time of the test
     */
    public void recordEnd(Instant startTime) {
        Span after = startSampledInSpan("after run");
        after.setAttribute(AttributeKey.longKey("succeeded"), successfulRuns.get());
        after.setAttribute(AttributeKey.longKey("failed"), failedRuns.get());
        after.setAttribute(AttributeKey.longKey("durationMs"), Instant.now().toEpochMilli() - startTime.toEpochMilli());
        after.end();

        // be sure to remove logging afterwards
        logger.atInfo()
            .addKeyValue("scenarioName", scenarioName)
            .addKeyValue("succeeded", successfulRuns.get())
            .addKeyValue("failed", failedRuns.get())
            .log("test finished");
    }


    private Span startSampledInSpan(String name) {
        return tracer.spanBuilder(name)
            // guarantee that we have before/after spans sampled in
            // and record duration/result of the test
            .setAttribute(SAMPLE_IN_ATTRIBUTE, true)
            .startSpan();
    }

    private static double getDuration(Instant start) {
        return Math.max(0d, Instant.now().toEpochMilli() - start.toEpochMilli()) / 1000d;
    }

    @FunctionalInterface
    public interface ThrowingFunction {
        void run(com.azure.core.util.Context context) throws Exception;
    }

}
