// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.ClientLongHistogram;
import com.azure.core.util.metrics.ClientMeter;
import com.azure.core.util.metrics.ClientMeterProvider;
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
import java.time.Duration;
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

    private static final SdkMeterProvider SDK_METER_PROVIDER = SdkMeterProvider.builder()
        .registerMetricReader(SDK_METER_READER)
        .build();

    private final static Map<String, Object> COMMON_ATTRIBUTES = new HashMap<String, Object>() {{
        put("az.messaging.destination", "fqdn");
        put("az.messaging.entity", "entityName");
    }};

    private final static MetricHelper METRIC_HELPER = new MetricHelper(ClientMeterProvider
        .createMeter("bench", null, new MetricsOptions().setImplementationConfiguration(SDK_METER_PROVIDER)),"fqdn", "entityName");

    private static final ClientLongHistogram HISTOGRAM_WITH_ATTRIBUTES = ClientMeterProvider
        .createMeter("bench", null, new MetricsOptions().setImplementationConfiguration(SDK_METER_PROVIDER))
        .getLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static final ClientLongHistogram HISTOGRAM = ClientMeterProvider
        .createMeter("bench", null, new MetricsOptions().setImplementationConfiguration(SDK_METER_PROVIDER))
        .getLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static final ClientLongHistogram NOOP_HISTOGRAM = ClientMeterProvider
        .createMeter("bench", null, new MetricsOptions().setImplementationConfiguration(io.opentelemetry.api.metrics.MeterProvider.noop()))
        .getLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static final ClientLongHistogram DISABLED_METRICS_HISTOGRAM = ClientMeterProvider
        .createMeter("bench", null, new MetricsOptions().setImplementationConfiguration(SDK_METER_READER).enable(false))
        .getLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static final Context AZ_CONTEXT_WITH_OTEL_CONTEXT = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.root());

    @Benchmark
    public void disabledMetrics() {
        DISABLED_METRICS_HISTOGRAM.record(1, AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    @Benchmark
    public void noopMeterProvider() {
        NOOP_HISTOGRAM.record(1, AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    @Benchmark
    public void basicHistogram() {
        HISTOGRAM.record(1, AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    @Benchmark
    public void basicHistogramWithCommonAttributes() {
        HISTOGRAM_WITH_ATTRIBUTES.record(1, AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    @Benchmark
    public void basicHistogramWithCommonAndExtraAttributes() {
        METRIC_HELPER.recordSendBatch(Duration.ofMillis(1), "pId", false, null, AZ_CONTEXT_WITH_OTEL_CONTEXT);
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }

    enum ErrorCode {
        ERR1,
        ERR2
    }

    static class MetricHelper {
        private final static int ERROR_DIMENSIONS_LENGTH = ErrorCode.values().length + 2;
        private final static String DURATION_METRIC_NAME = "az.messaging.producer.send.duration";
        private final static String DURATION_METRIC_DESCRIPTION = "Duration of producer send call";
        private final static String DURATION_METRIC_UNIT = "ms";

        private final ClientMeter meter;
        private final String fullyQualifiedNamespace;
        private final String eventHubName;

        public MetricHelper(ClientMeter meter, String fullyQualifiedNamespace, String eventHubName) {
            this.meter = meter;
            this.fullyQualifiedNamespace = fullyQualifiedNamespace;
            this.eventHubName = eventHubName;
        }

        // do we know all partitions ahead of time?
        private final ConcurrentMap<String, SendBatchMetrics[]> allMetrics = new ConcurrentHashMap<>();

        void recordSendBatch(Duration duration, String partitionId, boolean error, ErrorCode errorCode, Context context) {
            SendBatchMetrics[] metrics = allMetrics.computeIfAbsent(partitionId, pId -> createMetrics(pId));

            int index = ERROR_DIMENSIONS_LENGTH - 1; // ok
            if (error) {
                index = errorCode != null ? errorCode.ordinal() : ERROR_DIMENSIONS_LENGTH - 2;
            }

            metrics[index].record(duration.toMillis(), context);
        }

        private SendBatchMetrics[] createMetrics(String partitionId) {
            SendBatchMetrics[] metrics = new SendBatchMetrics[ERROR_DIMENSIONS_LENGTH];
            for (int i = 0; i < ERROR_DIMENSIONS_LENGTH - 2; i ++) {
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
            public final ClientLongHistogram sendDuration;

            public SendBatchMetrics(ClientMeter meter, Map<String, Object> attributes) {
                this.sendDuration  = meter.getLongHistogram(DURATION_METRIC_NAME, DURATION_METRIC_DESCRIPTION, DURATION_METRIC_UNIT, attributes);
            }

            public void record(long duration, Context context) {
                sendDuration.record(duration, context);
            }
        }
    }
}
