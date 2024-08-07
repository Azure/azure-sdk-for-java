// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.AbstractTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricPointBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.DependencyExtractor;
import com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.RequestExtractor;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.FormattedTime;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.resources.Resource;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.monitor.opentelemetry.exporter.implementation.AiSemanticAttributes.IS_SYNTHETIC;
import static com.azure.monitor.opentelemetry.exporter.implementation.SpanDataMapper.getStableOrOldAttribute;
import static com.azure.monitor.opentelemetry.exporter.implementation.MappingsBuilder.MappingType.METRIC;
import static io.opentelemetry.api.internal.Utils.checkArgument;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_SUM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.HISTOGRAM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_SUM;

public class MetricDataMapper {

    private static final ClientLogger logger = new ClientLogger(MetricDataMapper.class);

    private static final Set<String> OTEL_UNSTABLE_METRICS_TO_EXCLUDE = new HashSet<>();
    private static final String OTEL_INSTRUMENTATION_NAME_PREFIX = "io.opentelemetry";
    private static final Set<String> OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES = new HashSet<>(4);
    public static final AttributeKey<String> APPLICATIONINSIGHTS_INTERNAL_METRIC_NAME = AttributeKey.stringKey("applicationinsights.internal.metric_name");

    private final BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer;
    private final boolean captureHttpServer4xxAsError;

    static {
        // HTTP unstable metrics to be excluded via Otel auto instrumentation
        OTEL_UNSTABLE_METRICS_TO_EXCLUDE.add("rpc.client.duration");
        OTEL_UNSTABLE_METRICS_TO_EXCLUDE.add("rpc.server.duration");

        // Application Insights pre-aggregated standard metrics
        OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.add("http.server.request.duration");
        OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.add("http.client.request.duration");
        OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.add("rpc.client.duration");
        OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.add("rpc.server.duration");
    }

    public MetricDataMapper(
        BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer,
        boolean captureHttpServer4xxAsError) {
        this.telemetryInitializer = telemetryInitializer;
        this.captureHttpServer4xxAsError = captureHttpServer4xxAsError;
    }

    public void map(MetricData metricData, Consumer<TelemetryItem> consumer) {
        MetricDataType type = metricData.getType();
        if (type == DOUBLE_SUM
            || type == DOUBLE_GAUGE
            || type == LONG_SUM
            || type == LONG_GAUGE
            || type == HISTOGRAM) {
            boolean isPreAggregatedStandardMetric =
                OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.contains(metricData.getName());
            if (isPreAggregatedStandardMetric) {
                List<TelemetryItem> preAggregatedStandardMetrics =
                    convertOtelMetricToAzureMonitorMetric(metricData, true);
                preAggregatedStandardMetrics.forEach(consumer::accept);
            }

            // DO NOT emit unstable metrics from the OpenTelemetry auto instrumentation libraries
            // custom metrics are always emitted
            if (OTEL_UNSTABLE_METRICS_TO_EXCLUDE.contains(metricData.getName())
                && metricData.getInstrumentationScopeInfo().getName().startsWith(OTEL_INSTRUMENTATION_NAME_PREFIX)) {
                return;
            }
            List<TelemetryItem> stableOtelMetrics = convertOtelMetricToAzureMonitorMetric(metricData, false);
            stableOtelMetrics.forEach(consumer::accept);
        } else {
            logger.warning("metric data type {} is not supported yet.", metricData.getType());
        }
    }

    private List<TelemetryItem> convertOtelMetricToAzureMonitorMetric(
        MetricData metricData, boolean isPreAggregatedStandardMetric) {
        List<TelemetryItem> telemetryItems = new ArrayList<>();

        for (PointData pointData : metricData.getData().getPoints()) {
            MetricTelemetryBuilder builder = MetricTelemetryBuilder.create();
            telemetryInitializer.accept(builder, metricData.getResource());

            builder.setTime(FormattedTime.offSetDateTimeFromEpochNanos(pointData.getEpochNanos()));
            updateMetricPointBuilder(
                builder,
                metricData,
                pointData,
                captureHttpServer4xxAsError,
                isPreAggregatedStandardMetric);

            telemetryItems.add(builder.build());
        }
        return telemetryItems;
    }

    // visible for testing
    public static void updateMetricPointBuilder(
        MetricTelemetryBuilder metricTelemetryBuilder,
        MetricData metricData,
        PointData pointData,
        boolean captureHttpServer4xxAsError,
        boolean isPreAggregatedStandardMetric) {
        checkArgument(metricData != null, "MetricData cannot be null.");

        MetricPointBuilder pointBuilder = new MetricPointBuilder();
        MetricDataType type = metricData.getType();
        double pointDataValue;
        switch (type) {
            case LONG_SUM:
            case LONG_GAUGE:
                pointDataValue = (double) ((LongPointData) pointData).getValue();
                break;
            case DOUBLE_SUM:
            case DOUBLE_GAUGE:
                pointDataValue = ((DoublePointData) pointData).getValue();
                break;
            case HISTOGRAM:
                long histogramCount = ((HistogramPointData) pointData).getCount();
                if (histogramCount <= Integer.MAX_VALUE && histogramCount >= Integer.MIN_VALUE) {
                    pointBuilder.setCount((int) histogramCount);
                }
                HistogramPointData histogramPointData = (HistogramPointData) pointData;
                double min = histogramPointData.getMin();
                double max = histogramPointData.getMax();
                if (shouldConvertToMilliseconds(metricData.getName(), isPreAggregatedStandardMetric)) {
                    min = min * 1000;
                    max = max * 1000;
                }
                pointDataValue = histogramPointData.getSum();
                pointBuilder.setMin(min);
                pointBuilder.setMax(max);
                break;
            case SUMMARY: // not supported yet in OpenTelemetry SDK
            case EXPONENTIAL_HISTOGRAM: // not supported yet in OpenTelemetry SDK
            default:
                throw new IllegalArgumentException("metric data type '" + type + "' is not supported yet");
        }

        // new http semconv metrics use seconds, but we want to send milliseconds to Breeze
        if (shouldConvertToMilliseconds(metricData.getName(), isPreAggregatedStandardMetric)) {
            pointDataValue = pointDataValue * 1000;
        }

        pointBuilder.setValue(pointDataValue);

        // We emit some metrics via OpenTelemetry that have names which use characters that aren't
        // supported in OpenTelemetry metric names, and so we put the real metric names into an attribute
        // (where these characters are supported) and then pull the name back out when sending it to Breeze.
        String metricName = pointData.getAttributes().get(APPLICATIONINSIGHTS_INTERNAL_METRIC_NAME);
        if (metricName != null) {
            pointBuilder.setName(metricName);
        } else {
            pointBuilder.setName(metricData.getName());
        }

        metricTelemetryBuilder.setMetricPoint(pointBuilder);

        Attributes attributes = pointData.getAttributes();
        if (isPreAggregatedStandardMetric) {
            Long statusCode = getStableOrOldAttribute(attributes, SemanticAttributes.HTTP_RESPONSE_STATUS_CODE, SemanticAttributes.HTTP_STATUS_CODE);
            boolean success = isSuccess(metricData.getName(), statusCode, captureHttpServer4xxAsError);
            Boolean isSynthetic = attributes.get(IS_SYNTHETIC);

            attributes.forEach(
                (key, value) ->
                    applyConnectionStringAndRoleNameOverrides(
                        metricTelemetryBuilder, value, key.getKey()));

            if (isServer(metricData.getName())) {
                RequestExtractor.extract(metricTelemetryBuilder, statusCode, success, isSynthetic);
            } else if (isClient(metricData.getName())) {
                String dependencyType;
                int defaultPort;
                if (metricData.getName().startsWith("http")) {
                    dependencyType = "Http";
                    defaultPort = getDefaultPortForHttpScheme(getStableOrOldAttribute(attributes, SemanticAttributes.URL_SCHEME, SemanticAttributes.HTTP_SCHEME));
                } else {
                    dependencyType = attributes.get(SemanticAttributes.RPC_SYSTEM);
                    if (dependencyType == null) {
                        // rpc.system is required by the semantic conventions
                        dependencyType = "Unknown";
                    }
                    defaultPort = Integer.MAX_VALUE; // no default port for rpc
                }
                String target = SpanDataMapper.getTargetOrDefault(attributes, defaultPort, dependencyType);
                DependencyExtractor.extract(
                    metricTelemetryBuilder, statusCode, success, dependencyType, target, isSynthetic);
            }
        } else {
            MappingsBuilder mappingsBuilder = new MappingsBuilder(METRIC);
            mappingsBuilder.build().map(attributes, metricTelemetryBuilder);
        }
    }

    private static boolean shouldConvertToMilliseconds(String metricName, boolean isPreAggregatedStandardMetric) {
        return isPreAggregatedStandardMetric && (metricName.equals("http.server.request.duration") || metricName.equals("http.client.request.duration"));
    }

    private static boolean applyConnectionStringAndRoleNameOverrides(
        AbstractTelemetryBuilder telemetryBuilder, Object value, String key) {
        if (key.equals(AiSemanticAttributes.INTERNAL_CONNECTION_STRING.getKey())
            && value instanceof String) {
            // intentionally letting exceptions from parse bubble up
            telemetryBuilder.setConnectionString(ConnectionString.parse((String) value));
            return true;
        }
        if (key.equals(AiSemanticAttributes.INTERNAL_ROLE_NAME.getKey()) && value instanceof String) {
            telemetryBuilder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), (String) value);
            return true;
        }
        return false;
    }

    private static int getDefaultPortForHttpScheme(@Nullable String httpScheme) {
        if (httpScheme == null) {
            return Integer.MAX_VALUE;
        }
        if (httpScheme.equals("https")) {
            return 443;
        }
        if (httpScheme.equals("http")) {
            return 80;
        }
        return Integer.MAX_VALUE;
    }

    // https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-spans.md#status
    private static boolean isSuccess(String metricName, Long statusCode, boolean captureHttpServer4xxAsError) {
        if (statusCode == null) {
            return true;
        }

        if (isClient(metricName)) {
            return statusCode < 400;
        }

        if (isServer(metricName)) {
            if (captureHttpServer4xxAsError) {
                return statusCode < 400;
            }
            return statusCode < 500;
        }

        return false;
    }

    private static boolean isClient(String metricName) {
        return metricName.contains(".client.");
    }

    private static boolean isServer(String metricName) {
        return metricName.contains(".server.");
    }
}
