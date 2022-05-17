// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.micrometer;

import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.AzureLongHistogram;
import com.azure.core.util.metrics.AzureMeter;
import com.azure.core.util.metrics.AzureMeterProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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

@Fork(3)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 2, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class MicrometerMetricsBenchmark {

    private static final MeterRegistry registry = new SimpleMeterRegistry();
    private final static AzureMeterProvider CLIENT_METER_PROVIDER = AzureMeterProvider.getDefaultProvider();

    private final static Map<String, Object> COMMON_ATTRIBUTES = new HashMap<>() {{
        put("az.messaging.destination", "fqdn");
        put("az.messaging.entity", "entityName");
    }};

    private static final MetricHelper METRIC_HELPER = new MetricHelper(CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(registry)),
        "fqdn", "entityName");

    private static final AzureLongHistogram HISTOGRAM_WITH_ATTRIBUTES = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(registry))
        .createLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static AzureLongHistogram HISTOGRAM = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(registry))
        .createLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    private static AzureLongHistogram DISABLED_METRICS_HISTOGRAM = CLIENT_METER_PROVIDER
        .createMeter("bench", null, new MetricsOptions().setProvider(registry).enable(false))
        .createLongHistogram("test", "description", "unit", COMMON_ATTRIBUTES);

    @Benchmark
    public void disabledMetrics() {
        DISABLED_METRICS_HISTOGRAM.record(1, Context.NONE);
    }

    @Benchmark
    public void basicHistogram() {
        HISTOGRAM.record(1, Context.NONE);
    }

    @Benchmark
    public void basicHistogramWithCommonAttributes() {
        HISTOGRAM_WITH_ATTRIBUTES.record(1, Context.NONE);
    }

    @Benchmark
    public void basicHistogramWithCommonAndExtraAttributes() {
        METRIC_HELPER.recordSendBatch(Duration.ofMillis(1), "pId", false, null, Context.NONE);
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

        private final AzureMeter meter;
        private final String fullyQualifiedNamespace;
        private final String eventHubName;

        public MetricHelper(AzureMeter meter, String fullyQualifiedNamespace, String eventHubName) {
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
            public final AzureLongHistogram sendDuration;

            public SendBatchMetrics(AzureMeter meter, Map<String, Object> attributes) {
                this.sendDuration  = meter.createLongHistogram(DURATION_METRIC_NAME, DURATION_METRIC_DESCRIPTION, DURATION_METRIC_UNIT, attributes);
            }

            public void record(long duration, Context context) {
                sendDuration.record(duration, context);
            }
        }
    }
}
