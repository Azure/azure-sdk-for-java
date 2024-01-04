// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.template.stress.util;

import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.sdk.template.stress.StressOptions;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Telemetry helper is used to monitor test execution and record stats.
 */
public class TelemetryHelper {
    private final Tracer tracer;
    private final ClientLogger logger;
    private static final AttributeKey<String> SCENARIO_NAME_ATTRIBUTE = AttributeKey.stringKey("scenario_name");
    private static final AttributeKey<String> ERROR_TYPE_ATTRIBUTE = AttributeKey.stringKey("error.type");
    private final String scenarioName;
    private final Meter meter;
    private final DoubleHistogram runDuration;
    private final Attributes commonAttributes;
    private final Attributes canceledAttributes;

    static {
        // enables micrometer metrics from Reactor schedulers allowing to monitor thread pool usage and starvation
        Schedulers.enableMetrics();
    }

    /**
     * Creates an instance of telemetry helper.
     * @param scenarioClass the scenario class
     */
    public TelemetryHelper(Class<?> scenarioClass) {
        this.scenarioName = scenarioClass.getName();
        this.tracer = GlobalOpenTelemetry.getTracer(scenarioName);
        this.meter = GlobalOpenTelemetry.getMeter(scenarioName);
        this.logger = new ClientLogger(scenarioName);
        this.runDuration = meter.histogramBuilder("test.run.duration")
            .setUnit("s")
            .build();
        this.commonAttributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName);
        this.canceledAttributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName, ERROR_TYPE_ATTRIBUTE, "cancelled");
    }

    /**
     * Instruments a runnable: records runnable duration along with the status (success, error, cancellation),
     * @param oneRun the runnable to instrument
     */
    @SuppressWarnings("try")
    public void instrumentRun(Runnable oneRun) {
        Instant start = Instant.now();
        Span span = tracer.spanBuilder("run").startSpan();
        try (Scope s = span.makeCurrent()) {
            oneRun.run();
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
    public Mono<Void> instrumentRunAsync(Mono<Void> runAsync) {
        return Mono.defer(() -> {
            Instant start = Instant.now();
            Span span = tracer.spanBuilder("runAsync").startSpan();
            try (Scope s = span.makeCurrent()) {
                return runAsync.doOnError(e -> trackFailure(start, e, span))
                    .doOnCancel(() -> trackCancellation(start, span))
                    .doOnSuccess(v -> trackSuccess(start, span))
                    .contextWrite(reactor.util.context.Context.of(com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current()));
            }
        });
    }

    private void trackSuccess(Instant start, Span span) {
        logger.atInfo()
            .log("run ended");

        runDuration.record(getDuration(start), commonAttributes);
        span.end();
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
        span.setStatus(StatusCode.ERROR, unwrapped.getMessage());

        String errorType = unwrapped.getClass().getName();
        logger.atError()
            .addKeyValue("error.type", errorType)
            .log("run ended", unwrapped);

        Attributes attributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName, ERROR_TYPE_ATTRIBUTE, errorType);
        runDuration.record(getDuration(start), attributes);
        span.end();
    }

    /**
     * Records an event representing the start of a test along with test options.
     * @param options test parameters
     */
    public void recordStart(StressOptions options) {
        String libraryPackageVersion = "unknown";
        try {
            Class<?> libraryPackage = Class.forName(HttpClientProvider.class.getName());
            libraryPackageVersion = libraryPackage.getPackage().getImplementationVersion();
            if (libraryPackageVersion == null) {
                libraryPackageVersion = "null";
            }
        } catch (ClassNotFoundException e) {
            logger.atWarning()
                .addKeyValue("class", HttpClientProvider.class.getName())
                .log("could not find class", e);
        }

        Span before = startSampledInSpan("before run");
        before.setAttribute(AttributeKey.longKey("durationSec"), options.getDuration());
        before.setAttribute(AttributeKey.stringKey("scenarioName"), scenarioName);
        before.setAttribute(AttributeKey.longKey("concurrency"), options.getParallel());
        before.setAttribute(AttributeKey.stringKey("libraryPackageVersion"), libraryPackageVersion);
        before.setAttribute(AttributeKey.booleanKey("sync"), options.isSync());
        before.setAttribute(AttributeKey.longKey("size"), options.getSize());
        before.setAttribute(AttributeKey.stringKey("hostname"), System.getenv().get("HOSTNAME"));
        before.setAttribute(AttributeKey.stringKey("serviceEndpoint"), options.getServiceEndpoint());
        before.setAttribute(AttributeKey.stringKey("httpClientProvider"), options.getHttpClient().toString());
        before.end();
    }

    /**
     * Records an event representing the end of the test.
     * @param startTime the start time of the test
     */
    public void recordEnd(Instant startTime) {
        Span after = startSampledInSpan("after run");
        after.setAttribute(AttributeKey.longKey("durationMs"), Instant.now().toEpochMilli() - startTime.toEpochMilli());
        after.end();
    }


    private Span startSampledInSpan(String name) {
        return tracer.spanBuilder(name)
            // guarantee that we have before/after spans sampled in
            // and record duration/result of the test
            .setAttribute("sample.in", "true")
            .startSpan();
    }

    private static double getDuration(Instant start) {
        return (Instant.now().toEpochMilli() - start.toEpochMilli()) / 1000d;
    }
}
