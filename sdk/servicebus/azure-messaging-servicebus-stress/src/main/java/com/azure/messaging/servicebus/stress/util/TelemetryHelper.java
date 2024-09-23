// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.util;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.monitor.opentelemetry.exporter.AzureMonitorCustomizer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
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
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Telemetry helper is used to monitor test execution and record stats.
 */
public class TelemetryHelper {
    private final Tracer tracer;
    private final ClientLogger logger;
    private final static OpenTelemetry OTEL;
    private static final AttributeKey<String> ERROR_TYPE_ATTRIBUTE = AttributeKey.stringKey("error.type");
    private static final AttributeKey<Boolean> SAMPLE_IN_ATTRIBUTE = AttributeKey.booleanKey("sample.in");
    private final String scenarioName;
    private final Meter meter;
    private final LongCounter errorCounter;

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
        if (applicationInsightsConnectionString != null) {
            AzureMonitorCustomizer.customize(sdkBuilder, applicationInsightsConnectionString);
        }

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

    public void initLogging() {
        // needs to re-init logging after Spring starts :(
        OpenTelemetryAppender.install(OTEL);
    }

    /**
     * Records an event representing the start of a test along with test options.
     * @param options test parameters
     */
    public void recordOptions(Span span, ScenarioOptions options) {
        String libraryPackageVersion = "unknown";
        try {
            Class<?> libraryPackage = Class.forName(ServiceBusClientBuilder.class.getName());
            libraryPackageVersion = libraryPackage.getPackage().getImplementationVersion();
            if (libraryPackageVersion == null) {
                libraryPackageVersion = "null";
            }
        } catch (ClassNotFoundException e) {
            logger.atWarning()
                .addKeyValue("class", ServiceBusClientBuilder.class.getName())
                .log("Could not determine azure-messaging-servicebus version, ServiceBusClientBuilder class is not found", e);
        }

        span.setAttribute(AttributeKey.longKey("durationSec"), options.getTestDuration().getSeconds());
        span.setAttribute(AttributeKey.stringKey("scenarioName"), scenarioName);
        span.setAttribute(AttributeKey.stringKey("serviceBusPackageVersion"), libraryPackageVersion);
        span.setAttribute(AttributeKey.longKey("messageSize"), options.getMessageSize());
        span.setAttribute(AttributeKey.stringKey("hostname"), System.getenv().get("HOSTNAME"));
        span.setAttribute(AttributeKey.stringKey("jreVersion"), System.getProperty("java.version"));
        span.setAttribute(AttributeKey.stringKey("jreVendor"), System.getProperty("java.vendor"));
        span.setAttribute(AttributeKey.stringKey("tryTimeout"), options.getTryTimeout().toString());
        span.setAttribute(AttributeKey.stringKey("entityType"), options.getServiceBusEntityType().toString());
        span.setAttribute(AttributeKey.stringKey("queueName"), options.getServiceBusQueueName());
        span.setAttribute(AttributeKey.stringKey("topicName"), options.getServiceBusTopicName());
        span.setAttribute(AttributeKey.stringKey("sessionQueueName"), options.getServiceBusSessionQueueName());
        span.setAttribute(AttributeKey.stringKey("subscriptionName"), options.getServiceBusSubscriptionName());
    }

    public Span startSampledInSpan(String name) {
        return tracer.spanBuilder(name)
            // guarantee that we have before/after spans sampled in
            // and record duration/result of the test
            .setAttribute(SAMPLE_IN_ATTRIBUTE, true)
            .startSpan();
    }


    public void recordError(String errorReason, String method) {
        recordError(errorReason, null, method);
    }

    public <T extends Throwable> void recordError(T ex, String method) {
        Throwable unwrapped = Exceptions.unwrap(ex);
        String errorReason = unwrapped.getClass().getName();
        if (unwrapped instanceof ServiceBusException) {
            errorReason = ((ServiceBusException) unwrapped).getReason().toString();
        }
        recordError(errorReason, unwrapped, method);
    }

    private void recordError(String errorReason, Throwable ex, String method) {
        AttributesBuilder attributesBuilder = Attributes.builder()
            .put(ERROR_TYPE_ATTRIBUTE, errorReason)
            .put(AttributeKey.stringKey("method"), method);

        errorCounter.add(1, attributesBuilder.build());
        LoggingEventBuilder log = logger.atError()
            .addKeyValue("error.type", errorReason)
            .addKeyValue("method", method);
        if (ex != null) {
            log.log("test error", ex);
        } else {
            log.log("test error");
        }
    }
}
