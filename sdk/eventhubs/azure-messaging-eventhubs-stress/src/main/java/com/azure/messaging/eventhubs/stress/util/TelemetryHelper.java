// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.monitor.opentelemetry.AzureMonitor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
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

/**
 * Telemetry helper is used to monitor test execution and record stats.
 */
public class TelemetryHelper {
    private final Tracer tracer;
    private final ClientLogger logger;
    private static final OpenTelemetry OTEL;
    private static final AttributeKey<String> SCENARIO_NAME_ATTRIBUTE = AttributeKey.stringKey("scenario_name");
    private static final AttributeKey<String> ERROR_TYPE_ATTRIBUTE = AttributeKey.stringKey("error.type");
    private static final AttributeKey<Boolean> SAMPLE_IN_ATTRIBUTE = AttributeKey.booleanKey("sample.in");
    private final String scenarioName;
    private final Meter meter;
    private final LongCounter closedPartitionCounter;
    private final LongCounter initializedPartitionCounter;
    private final LongCounter errorCounter;

    private final DoubleHistogram runDuration;
    private final Attributes commonAttributes;
    private final Attributes canceledAttributes;

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
        this.closedPartitionCounter = meter.counterBuilder("partition_closed").build();
        this.initializedPartitionCounter = meter.counterBuilder("partition_initialized").build();
        this.errorCounter = meter.counterBuilder("test.run.errors").build();
    }

    /**
     * Initializes telemetry helper: sets up Azure Monitor exporter, enables JVM metrics collection.
     */
    private static OpenTelemetry init() {
        String applicationInsightsConnectionString = System.getenv("APPLICATIONINSIGHTS_CONNECTION_STRING");
        if (applicationInsightsConnectionString == null) {
            return OpenTelemetry.noop();
        }
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        AzureMonitor.customize(sdkBuilder, applicationInsightsConnectionString);

        String instanceId = System.getenv("CONTAINER_NAME");
        OpenTelemetry otel = sdkBuilder
                .addResourceCustomizer((resource, props) ->
                        instanceId == null ? resource : resource.toBuilder().put(AttributeKey.stringKey("service.instance.id"), instanceId).build())
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
     * Re-initializes logging to otel - necessary in spring applications
     */
    public void initLogging() {
        // need to re-init logging after Spring starts :(
        OpenTelemetryAppender.install(OTEL);
    }

    /**
     * Instruments a runnable: records runnable duration along with the status (success, error, cancellation),
     * @param oneRun the runnable to instrument
     * @param method the method name
     * @param partitionId the partition id
     */
    @SuppressWarnings("try")
    public void instrumentProcess(Runnable oneRun, String method, String partitionId) {
        Instant start = Instant.now();
        Span span = tracer.spanBuilder(method).startSpan();
        try (Scope s = span.makeCurrent()) {
            oneRun.run();
            trackSuccess(start, span);
        } catch (Throwable e) {
            if (e.getMessage().contains("Timeout on blocking read") || e instanceof InterruptedException || e instanceof TimeoutException) {
                trackCancellation(start, span);
            } else {
                trackFailure(start, e, span, method, partitionId);
            }
            throw e;
        }
    }

    /**
     * Instruments a Mono: records mono duration along with the status (success, error, cancellation),
     * @param runAsync the mono to instrument
     * @param method the method name
     * @return the instrumented mono
     */
    @SuppressWarnings("try")
    public Mono<Void> instrumentRunAsync(Mono<Void> runAsync, String method) {
        return Mono.defer(() -> {
            Instant start = Instant.now();
            Span span = tracer.spanBuilder(method).startSpan();
            try (Scope s = span.makeCurrent()) {
                return runAsync.doOnError(e -> trackFailure(start, e, span, method, null))
                    .doOnCancel(() -> trackCancellation(start, span))
                    .doOnSuccess(v -> trackSuccess(start, span))
                    .contextWrite(reactor.util.context.Context.of(com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY, Context.current()))
                    .onErrorResume(e -> Mono.empty());
            }
        });
    }

    private void trackSuccess(Instant start, Span span) {
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

    private void trackFailure(Instant start, Throwable e, Span span, String method, String partitionId) {
        Throwable unwrapped = Exceptions.unwrap(e);

        span.recordException(unwrapped);
        span.setStatus(StatusCode.ERROR, unwrapped.getMessage());
        String errorType = unwrapped.getClass().getName();
        recordError(errorType, unwrapped, method, partitionId);
        Attributes attributes = Attributes.of(SCENARIO_NAME_ATTRIBUTE, scenarioName, ERROR_TYPE_ATTRIBUTE, errorType);
        runDuration.record(getDuration(start), attributes);
        span.end();
    }

    /**
     * Records an event representing the start of a test along with test options.
     * @param span the span to record attributes on
     * @param options test parameters
     */
    public void recordOptions(Span span, ScenarioOptions options) {
        String libraryPackageVersion = "unknown";
        try {
            Class<?> libraryPackage = Class.forName(EventHubClientBuilder.class.getName());
            libraryPackageVersion = libraryPackage.getPackage().getImplementationVersion();
            if (libraryPackageVersion == null) {
                libraryPackageVersion = "null";
            }
        } catch (ClassNotFoundException e) {
            logger.atWarning()
                .addKeyValue("class", EventHubClientBuilder.class.getName())
                .log("Could not determine azure-eventhubs-messaging version, EventHubClientBuilder class is not found", e);
        }

        span.setAttribute(AttributeKey.longKey("durationSec"), options.getTestDuration().getSeconds());
        span.setAttribute(AttributeKey.stringKey("scenarioName"), scenarioName);
        span.setAttribute(AttributeKey.stringKey("packageVersion"), libraryPackageVersion);
        span.setAttribute(AttributeKey.stringKey("eventHubName"), options.getEventHubsEventHubName());
        span.setAttribute(AttributeKey.stringKey("consumerGroupName"), options.getEventHubsConsumerGroup());
        span.setAttribute(AttributeKey.longKey("messageSize"), options.getMessageSize());
        span.setAttribute(AttributeKey.stringKey("hostname"), System.getenv().get("HOSTNAME"));
        span.setAttribute(AttributeKey.stringKey("jreVersion"), System.getProperty("java.version"));
        span.setAttribute(AttributeKey.stringKey("jreVendor"), System.getProperty("java.vendor"));
    }

    public Span startSampledInSpan(String name) {
        return tracer.spanBuilder(name)
            // guarantee that we have before/after spans sampled in
            // and record duration/result of the test
            .setAttribute(SAMPLE_IN_ATTRIBUTE, true)
            .startSpan();
    }


    public void recordError(String errorReason, String method, String partitionId) {
        recordError(errorReason, null, method, partitionId);
    }

    public <T extends Throwable> void recordError(T ex, String method, String partitionId) {
        Throwable unwrapped = Exceptions.unwrap(ex);
        recordError(unwrapped.getClass().getName(), unwrapped, method, partitionId);
    }

    private void recordError(String errorReason, Throwable ex, String method, String partitionId) {
        AttributesBuilder attributesBuilder = Attributes.builder()
            .put(AttributeKey.stringKey("error.type"), errorReason)
            .put(AttributeKey.stringKey("method"), method);

        if (partitionId != null) {
            attributesBuilder.put(AttributeKey.stringKey("partitionId"), partitionId);
        }

        errorCounter.add(1, attributesBuilder.build());
        LoggingEventBuilder log = logger.atError()
            .addKeyValue("partitionId", partitionId)
            .addKeyValue("error.type", errorReason)
            .addKeyValue("method", method);
        if (ex != null) {
            log.log("test error", ex);
        } else {
            log.log("test error");
        }
    }

    public void recordPartitionClosedEvent(CloseContext closeContext, String processorId) {
        Attributes attributes = Attributes.builder()
            .put(AttributeKey.stringKey("partition_id"), closeContext.getPartitionContext().getPartitionId())
            .put(AttributeKey.stringKey("closed_reason"), closeContext.getCloseReason().toString())
            .put(AttributeKey.stringKey("processor_id"), processorId)
            .build();
        closedPartitionCounter.add(1, attributes);
    }

    public void recordPartitionInitializedEvent(InitializationContext initializationContext, String processorId) {
        Attributes attributes = Attributes.builder()
            .put(AttributeKey.stringKey("partition_id"), initializationContext.getPartitionContext().getPartitionId())
            .put(AttributeKey.stringKey("processor_id"), processorId)
            .build();
        initializedPartitionCounter.add(1, attributes);
    }

    private static double getDuration(Instant start) {
        return Math.max(0d, Instant.now().toEpochMilli() - start.toEpochMilli()) / 1000d;
    }
}
