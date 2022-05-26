// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

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
import java.util.HashMap;
import java.util.Map;
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

    private static final Map<String, Object> COMMON_ATTRIBUTES = new HashMap<String, Object>() {{
            put("az.messaging.destination", "fqdn");
            put("az.messaging.entity", "entityName");
        }};

    private static final AzureMeter METER = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(SDK_METER_PROVIDER));

    private static final MetricHelper METRIC_HELPER = new MetricHelper(METER, "fqdn", "entityName");

    private static final AzureLongHistogram HISTOGRAM_WITH_ATTRIBUTES = METER
        .createLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static final AzureLongHistogram HISTOGRAM = METER
        .createLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static final AzureLongHistogram NOOP_HISTOGRAM = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(io.opentelemetry.api.metrics.MeterProvider.noop()))
        .createLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static final AzureMeter DISABLED_METER = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(SDK_METER_READER).enable(false));

    private static final AzureLongHistogram DISABLED_METRICS_HISTOGRAM = DISABLED_METER
        .createLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static final Context AZ_CONTEXT_WITH_OTEL_CONTEXT = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root());

    @Benchmark
    public void disabledOptimizedMetrics() {
        Instant startTime = null;
        if (DISABLED_METER.isEnabled()) {
            startTime = Instant.now();
        }

        // do stuff

        if (DISABLED_METER.isEnabled()) {
            DISABLED_METRICS_HISTOGRAM.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), AZ_CONTEXT_WITH_OTEL_CONTEXT);
        }
    }

    @Benchmark
    public void disabledNotOptimizedMetrics() {
        Instant startTime = Instant.now();
        DISABLED_METRICS_HISTOGRAM.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    @Benchmark
    public void noopMeterProviderNotOptimized() {
        long startTime = Instant.now().toEpochMilli();
        NOOP_HISTOGRAM.record(Instant.now().toEpochMilli() - startTime, AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    @Benchmark
    public void basicHistogram() {
        Instant startTime = null;
        if (METER.isEnabled()) {
            startTime = Instant.now();
        }

        // do stuff
        if (METER.isEnabled()) {
            HISTOGRAM.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), AZ_CONTEXT_WITH_OTEL_CONTEXT);
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
            HISTOGRAM_WITH_ATTRIBUTES.record(Instant.now().toEpochMilli() - startTime.toEpochMilli(), AZ_CONTEXT_WITH_OTEL_CONTEXT);
        }
    }

    @Benchmark
    public void basicHistogramWithCommonAndExtraAttributes() {
        Instant startTime = null;
        if (METER.isEnabled()) {
            startTime = Instant.now();
        }

        // do stuff
        if (METER.isEnabled()) {
            METRIC_HELPER.recordSendBatch(Instant.now().toEpochMilli() - startTime.toEpochMilli(), "pId", false, null, AZ_CONTEXT_WITH_OTEL_CONTEXT);
        }
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }

    enum ErrorCode {
        ERR1,
        ERR2
    }

    static class MetricHelper {
        private static final int ERROR_DIMENSIONS_LENGTH = ErrorCode.values().length + 2;
        private static final String DURATION_METRIC_NAME = "az.messaging.producer.send.duration";
        private static final String DURATION_METRIC_DESCRIPTION = "Duration of producer send call";
        private static final String DURATION_METRIC_UNIT = "ms";

        private final AzureMeter meter;
        private final String fullyQualifiedNamespace;
        private final String eventHubName;

        MetricHelper(AzureMeter meter, String fullyQualifiedNamespace, String eventHubName) {
            this.meter = meter;
            this.fullyQualifiedNamespace = fullyQualifiedNamespace;
            this.eventHubName = eventHubName;
        }

        // do we know all partitions ahead of time?
        private final ConcurrentMap<String, SendBatchMetrics[]> allMetrics = new ConcurrentHashMap<>();

        void recordSendBatch(long duration, String partitionId, boolean error, ErrorCode errorCode, Context context) {
            SendBatchMetrics[] metrics = allMetrics.computeIfAbsent(partitionId, this::createMetrics);

            int index = ERROR_DIMENSIONS_LENGTH - 1; // ok
            if (error) {
                index = errorCode != null ? errorCode.ordinal() : ERROR_DIMENSIONS_LENGTH - 2;
            }

            metrics[index].record(duration, context);
        }

        private SendBatchMetrics[] createMetrics(String partitionId) {
            SendBatchMetrics[] metrics = new SendBatchMetrics[ERROR_DIMENSIONS_LENGTH];
            for (int i = 0; i < ERROR_DIMENSIONS_LENGTH - 2; i++) {
                metrics[i] =  new SendBatchMetrics(meter,
                    getAttributes(partitionId, ErrorCode.values()[i].name()));
            }

            metrics[ERROR_DIMENSIONS_LENGTH - 2] = new SendBatchMetrics(meter, getAttributes(partitionId, "unknown"));
            metrics[ERROR_DIMENSIONS_LENGTH - 1] = new SendBatchMetrics(meter, getAttributes(partitionId, "ok"));
            return metrics;
        }

        private Map<String, Object> getAttributes(String partitionId, String errorCode) {
            Map<String, Object> attributes = new HashMap<>(4);
            attributes.put("az.messaging.destination", fullyQualifiedNamespace);
            attributes.put("az.messaging.entity", eventHubName);
            attributes.put("az.messaging.partition_id", partitionId);
            attributes.put("az.messaging.status_code", errorCode);

            return attributes;
        }

        private static class SendBatchMetrics {
            private final AzureLongHistogram sendDuration;

            SendBatchMetrics(AzureMeter meter, Map<String, Object> attributes) {
                this.sendDuration  = meter.createLongHistogram(DURATION_METRIC_NAME, DURATION_METRIC_DESCRIPTION, DURATION_METRIC_UNIT, attributes);
            }

            public void record(long duration, Context context) {
                sendDuration.record(duration, context);
            }
        }
    }
}
