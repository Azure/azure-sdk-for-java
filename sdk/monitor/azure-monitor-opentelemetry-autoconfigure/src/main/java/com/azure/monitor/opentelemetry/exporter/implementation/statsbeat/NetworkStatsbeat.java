// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemExporter;
import io.opentelemetry.api.internal.GuardedBy;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.Constant.EXCEPTION_TYPE;
import static com.azure.monitor.opentelemetry.exporter.implementation.utils.Constant.STATUS_CODE;

public class NetworkStatsbeat extends BaseStatsbeat {

    private static final String REQUEST_SUCCESS_COUNT_METRIC_NAME = "Request_Success_Count";
    private static final String REQUEST_FAILURE_COUNT_METRIC_NAME = "Request_Failure_Count";
    private static final String REQUEST_DURATION_METRIC_NAME = "Request_Duration";
    private static final String RETRY_COUNT_METRIC_NAME = "Retry_Count";
    private static final String THROTTLE_COUNT_METRIC_NAME = "Throttle_Count";
    private static final String EXCEPTION_COUNT_METRIC_NAME = "Exception_Count";
    private static final String BREEZE_ENDPOINT = "breeze";

    private static final Pattern hostPattern = Pattern.compile("^https?://(?:www\\.)?([^/.]+)");

    private final Object lock = new Object();

    @GuardedBy("lock")
    private final Map<IntervalMetricsKey, IntervalMetrics> instrumentationKeyCounterMap = new HashMap<>();

    // only used by tests
    NetworkStatsbeat() {
        super(new CustomDimensions());
    }

    NetworkStatsbeat(CustomDimensions customDimensions) {
        super(customDimensions);
    }

    @Override
    protected void send(TelemetryItemExporter telemetryItemExporter) {
        Map<IntervalMetricsKey, IntervalMetrics> local;
        synchronized (lock) {
            local = new HashMap<>(instrumentationKeyCounterMap);
            instrumentationKeyCounterMap.clear();
        }

        for (Map.Entry<IntervalMetricsKey, IntervalMetrics> entry : local.entrySet()) {
            IntervalMetricsKey key = entry.getKey();
            sendIntervalMetric(telemetryItemExporter, key, entry.getValue());
        }
    }

    public void incrementRequestSuccessCount(long duration, String ikey, String host) {
        doWithIntervalMetrics(ikey, host, null, null, intervalMetrics -> {
            intervalMetrics.requestSuccessCount.incrementAndGet();
            intervalMetrics.totalRequestDuration.getAndAdd(duration);
        });
    }

    public void incrementRequestFailureCount(String ikey, String host, String causeFieldName, int statusCode) {
        doWithIntervalMetrics(ikey, host, causeFieldName, statusCode,
            intervalMetrics -> intervalMetrics.requestFailureCount.incrementAndGet());
    }

    // TODO (heya) this is never called
    public void incrementRetryCount(String ikey, String host, String causeFieldName, int statusCode) {
        doWithIntervalMetrics(ikey, host, causeFieldName, statusCode,
            intervalMetrics -> intervalMetrics.retryCount.incrementAndGet());
    }

    public void incrementThrottlingCount(String ikey, String host, String causeFieldName, int statusCode) {
        doWithIntervalMetrics(ikey, host, causeFieldName, statusCode,
            intervalMetrics -> intervalMetrics.throttlingCount.incrementAndGet());
    }

    void incrementExceptionCount(String ikey, String host, String causeFieldName, String exceptionType) {
        doWithIntervalMetrics(ikey, host, causeFieldName, exceptionType,
            intervalMetrics -> intervalMetrics.exceptionCount.incrementAndGet());
    }

    // only used by tests
    long getRequestSuccessCount(String ikey, String host) {
        synchronized (lock) {
            IntervalMetrics intervalMetrics
                = instrumentationKeyCounterMap.get(IntervalMetricsKey.create(ikey, host, null, null));
            return intervalMetrics == null ? 0L : intervalMetrics.requestSuccessCount.get();
        }
    }

    // only used by tests
    long getRequestFailureCount(String ikey, String host, int statusCode) {
        synchronized (lock) {
            IntervalMetrics intervalMetrics
                = instrumentationKeyCounterMap.get(IntervalMetricsKey.create(ikey, host, STATUS_CODE, statusCode));
            return intervalMetrics == null ? 0L : intervalMetrics.requestFailureCount.get();
        }
    }

    // only used by tests
    double getRequestDurationAvg(String ikey, String host) {
        synchronized (lock) {
            IntervalMetrics intervalMetrics
                = instrumentationKeyCounterMap.get(IntervalMetricsKey.create(ikey, host, null, null));
            return intervalMetrics == null ? 0L : intervalMetrics.getRequestDurationAvg();
        }
    }

    // only used by tests
    long getRetryCount(String ikey, String host, int statusCode) {
        synchronized (lock) {
            IntervalMetrics intervalMetrics
                = instrumentationKeyCounterMap.get(IntervalMetricsKey.create(ikey, host, STATUS_CODE, statusCode));
            return intervalMetrics == null ? 0L : intervalMetrics.retryCount.get();
        }
    }

    // only used by tests
    long getThrottlingCount(String ikey, String host, int statusCode) {
        synchronized (lock) {
            IntervalMetrics intervalMetrics
                = instrumentationKeyCounterMap.get(IntervalMetricsKey.create(ikey, host, STATUS_CODE, statusCode));
            return intervalMetrics == null ? 0L : intervalMetrics.throttlingCount.get();
        }
    }

    // only used by tests
    long getExceptionCount(String ikey, String host, String exceptionType) {
        synchronized (lock) {
            IntervalMetrics intervalMetrics = instrumentationKeyCounterMap
                .get(IntervalMetricsKey.create(ikey, host, EXCEPTION_TYPE, exceptionType));
            return intervalMetrics == null ? 0L : intervalMetrics.exceptionCount.get();
        }
    }

    private void doWithIntervalMetrics(String ikey, String host, @Nullable String causeFieldName,
        @Nullable Object causeValue, Consumer<IntervalMetrics> update) {
        synchronized (lock) {
            IntervalMetrics intervalMetrics = instrumentationKeyCounterMap.computeIfAbsent(
                IntervalMetricsKey.create(ikey, host, causeFieldName, causeValue), k -> new IntervalMetrics());
            update.accept(intervalMetrics);
        }
    }

    private void sendIntervalMetric(TelemetryItemExporter telemetryItemExporter, IntervalMetricsKey key,
        IntervalMetrics local) {
        if (local.requestSuccessCount.get() != 0) {
            StatsbeatTelemetryBuilder requestSuccessCountSt
                = createStatsbeatTelemetry(REQUEST_SUCCESS_COUNT_METRIC_NAME, (double) local.requestSuccessCount.get());
            addCommonProperties(requestSuccessCountSt, key);
            telemetryItemExporter.send(Collections.singletonList(requestSuccessCountSt.build()));
        }

        if (local.requestFailureCount.get() != 0) {
            StatsbeatTelemetryBuilder requestFailureCountSt
                = createStatsbeatTelemetry(REQUEST_FAILURE_COUNT_METRIC_NAME, (double) local.requestFailureCount.get());
            addCommonProperties(requestFailureCountSt, key);
            telemetryItemExporter.send(Collections.singletonList(requestFailureCountSt.build()));
        }

        double durationAvg = local.getRequestDurationAvg();
        if (durationAvg != 0) {
            StatsbeatTelemetryBuilder requestDurationSt
                = createStatsbeatTelemetry(REQUEST_DURATION_METRIC_NAME, durationAvg);
            addCommonProperties(requestDurationSt, key);
            telemetryItemExporter.send(Collections.singletonList(requestDurationSt.build()));
        }

        if (local.retryCount.get() != 0) {
            StatsbeatTelemetryBuilder retryCountSt
                = createStatsbeatTelemetry(RETRY_COUNT_METRIC_NAME, (double) local.retryCount.get());
            addCommonProperties(retryCountSt, key);
            telemetryItemExporter.send(Collections.singletonList(retryCountSt.build()));
        }

        if (local.throttlingCount.get() != 0) {
            StatsbeatTelemetryBuilder throttleCountSt
                = createStatsbeatTelemetry(THROTTLE_COUNT_METRIC_NAME, (double) local.throttlingCount.get());
            addCommonProperties(throttleCountSt, key);
            telemetryItemExporter.send(Collections.singletonList(throttleCountSt.build()));
        }

        if (local.exceptionCount.get() != 0) {
            StatsbeatTelemetryBuilder exceptionCountSt
                = createStatsbeatTelemetry(EXCEPTION_COUNT_METRIC_NAME, (double) local.exceptionCount.get());
            addCommonProperties(exceptionCountSt, key);
            telemetryItemExporter.send(Collections.singletonList(exceptionCountSt.build()));
        }
    }

    private static void addCommonProperties(StatsbeatTelemetryBuilder telemetryBuilder, IntervalMetricsKey key) {
        telemetryBuilder.addProperty("endpoint", BREEZE_ENDPOINT);
        telemetryBuilder.addProperty("cikey", key.getIkey());
        telemetryBuilder.addProperty("host", shorten(key.getHost()));
        String causeFieldName = key.getCauseFieldName();
        Object cause = key.getCauseValue();
        if (causeFieldName != null && cause != null) {
            // track 'statusCode' for Failure/Retry/Throttle count and track 'exceptionType' for Exception
            // Count
            if (cause instanceof Integer) {
                telemetryBuilder.addProperty(causeFieldName, ((Integer) cause).toString());
            } else if (cause instanceof String) {
                telemetryBuilder.addProperty(causeFieldName, (String) cause);
            }
        }
    }

    private static class IntervalMetrics {
        private final AtomicLong requestSuccessCount = new AtomicLong();
        private final AtomicLong requestFailureCount = new AtomicLong();
        // request duration count only counts request success.
        private final AtomicLong totalRequestDuration = new AtomicLong(); // duration in milliseconds
        private final AtomicLong retryCount = new AtomicLong();
        private final AtomicLong throttlingCount = new AtomicLong();
        private final AtomicLong exceptionCount = new AtomicLong();

        private double getRequestDurationAvg() {
            double sum = totalRequestDuration.get();
            if (requestSuccessCount.get() != 0) {
                return sum / requestSuccessCount.get();
            }

            return sum;
        }
    }

    /**
     * e.g. endpointUrl 'https://westus-0.in.applicationinsights.azure.com/v2.1/track' host will
     * return 'westus-0'
     */
    static String shorten(String endpointUrl) {
        Matcher matcher = hostPattern.matcher(endpointUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // it's better to send bad endpointUrl to Statsbeat for troubleshooting.
        return endpointUrl;
    }
}
