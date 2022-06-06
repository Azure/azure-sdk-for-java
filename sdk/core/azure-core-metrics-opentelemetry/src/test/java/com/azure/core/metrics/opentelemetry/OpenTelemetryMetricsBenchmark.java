// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.AttributeBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.AzureLongHistogram;
import com.azure.core.util.metrics.AzureMeter;
import com.azure.core.util.metrics.AzureMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

@Fork(3)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 2, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class OpenTelemetryMetricsBenchmark {
    private static final InMemoryMetricReader SDK_METER_READER = InMemoryMetricReader.create();
    private static final AzureMeterProvider CLIENT_METER_PROVIDER = AzureMeterProvider.getDefaultProvider();

    private static final SdkMeterProvider SDK_METER_PROVIDER = SdkMeterProvider.builder()
        .registerMetricReader(SDK_METER_READER)
        .build();

    private static final AttributeBuilder COMMON_ATTRIBUTES = new OpenTelemetryAttributeBuilder()
        .addAttribute("az.messaging.destination", "fqdn")
        .addAttribute("az.messaging.entity", "entityName");

    private static final AzureMeter METER = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(SDK_METER_PROVIDER));

    private static final DynamicAttributeCache DYNAMIC_ATTRIBUTE_CACHE = new DynamicAttributeCache(METER, "fqdn", "entityName");

    private static final AzureLongHistogram HISTOGRAM = METER
        .createLongHistogram("test", "description", "unit");

    private static final AzureLongHistogram NOOP_HISTOGRAM = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(io.opentelemetry.api.metrics.MeterProvider.noop()))
        .createLongHistogram("test", "description", "unit");

    private static final AzureMeter DISABLED_METER = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(SDK_METER_READER).enable(false));

    private static final AzureLongHistogram DISABLED_METRICS_HISTOGRAM = DISABLED_METER
        .createLongHistogram("test", "description", "unit");

    private static final Context AZ_CONTEXT_WITH_OTEL_CONTEXT = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root());

    @Benchmark
    public void disabledOptimizedMetrics() {
        Instant startTime = null;
        if (DISABLED_METER.isEnabled()) {
            startTime = Instant.now();
        }

        // do stuff

        if (DISABLED_METER.isEnabled()) {
            DISABLED_METRICS_HISTOGRAM.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), COMMON_ATTRIBUTES, AZ_CONTEXT_WITH_OTEL_CONTEXT);
        }
    }

    @Benchmark
    public void disabledNotOptimizedMetrics() {
        Instant startTime = Instant.now();
        DISABLED_METRICS_HISTOGRAM.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), COMMON_ATTRIBUTES,  AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    @Benchmark
    public void noopMeterProviderNotOptimized() {
        long startTime = Instant.now().toEpochMilli();
        NOOP_HISTOGRAM.record(Instant.now().toEpochMilli() - startTime, COMMON_ATTRIBUTES, AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    @Benchmark
    public void basicHistogram() {
        Instant startTime = null;
        if (METER.isEnabled()) {
            startTime = Instant.now();
        }

        // do stuff
        if (METER.isEnabled()) {
            HISTOGRAM.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), null, AZ_CONTEXT_WITH_OTEL_CONTEXT);
        }
    }

    @Benchmark
    public void basicHistogramWithCommonAttributes() {
        Instant startTime = null;
        if (METER.isEnabled()) {
            startTime = Instant.now();
        }

        // do stuff
        if (METER.isEnabled()) {
            HISTOGRAM.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), COMMON_ATTRIBUTES, AZ_CONTEXT_WITH_OTEL_CONTEXT);
        }
    }

    @Benchmark
    public void basicHistogramWithDynamicAttributes() {
        Instant startTime = null;
        if (METER.isEnabled()) {
            startTime = Instant.now();
        }

        if (METER.isEnabled()) {
            HISTOGRAM.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), DYNAMIC_ATTRIBUTE_CACHE.getOrCreate("pId", false, null), AZ_CONTEXT_WITH_OTEL_CONTEXT);
        }
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }

    enum ErrorCode {
        ERR1,
        ERR2
    }

    static class DynamicAttributeCache {
        private static final int ERROR_DIMENSIONS_LENGTH = ErrorCode.values().length + 2;

        private final AzureMeter meter;
        private final String fullyQualifiedNamespace;
        private final String eventHubName;

        DynamicAttributeCache(AzureMeter meter, String fullyQualifiedNamespace, String eventHubName) {
            this.meter = meter;
            this.fullyQualifiedNamespace = fullyQualifiedNamespace;
            this.eventHubName = eventHubName;
        }

        private final ConcurrentMap<String, AttributeBuilder[]> allAttributes = new ConcurrentHashMap<>();

        AttributeBuilder getOrCreate(String partitionId, boolean error, ErrorCode errorCode) {
            AttributeBuilder[] attributes = allAttributes.computeIfAbsent(partitionId, this::createAttributes);

            int index = ERROR_DIMENSIONS_LENGTH - 1; // ok
            if (error) {
                index = errorCode != null ? errorCode.ordinal() : ERROR_DIMENSIONS_LENGTH - 2; // unknown
            }

            return attributes[index];
        }

        private AttributeBuilder[]  createAttributes(String partitionId) {
            AttributeBuilder[] attributes = new AttributeBuilder[ERROR_DIMENSIONS_LENGTH];
            for (int i = 0; i < ERROR_DIMENSIONS_LENGTH - 2; i++) {
                attributes[i] =  getAttributes(partitionId, ErrorCode.values()[i].name());
            }

            attributes[ERROR_DIMENSIONS_LENGTH - 2] = getAttributes(partitionId, "unknown");
            attributes[ERROR_DIMENSIONS_LENGTH - 1] = getAttributes(partitionId, "ok");
            return attributes;
        }

        private AttributeBuilder getAttributes(String partitionId, String errorCode) {
            return meter.createAttributesBuilder()
                .addAttribute("az.messaging.destination", fullyQualifiedNamespace)
                .addAttribute("az.messaging.entity", eventHubName)
                .addAttribute("az.messaging.partition_id", partitionId)
                .addAttribute("az.messaging.status_code", errorCode);
        }
    }
}
