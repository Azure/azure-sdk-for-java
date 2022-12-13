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
import static io.opentelemetry.api.internal.Utils.checkArgument;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_SUM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.HISTOGRAM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_SUM;

public class MetricDataMapper {

    private static final ClientLogger logger = new ClientLogger(MetricDataMapper.class);

    private static final Set<String> OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES = new HashSet<>(4);
    private static final List<String> EXCLUDED_METRIC_NAMES = new ArrayList<>();

    private static final Mappings MAPPINGS;

    private final BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer;
    private final boolean captureHttpServer4xxAsError;

    static {
        EXCLUDED_METRIC_NAMES.add("http.server.active_requests"); // Servlet

        OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.add("http.server.duration"); // Servlet
        OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.add("http.client.duration"); // HttpClient
        OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.add("rpc.client.duration"); // gRPC
        OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.add("rpc.server.duration"); // gRPC

        MAPPINGS = new MappingsBuilder().build();
    }

    public MetricDataMapper(
        BiConsumer<AbstractTelemetryBuilder, Resource> telemetryInitializer,
        boolean captureHttpServer4xxAsError) {
        this.telemetryInitializer = telemetryInitializer;
        this.captureHttpServer4xxAsError = captureHttpServer4xxAsError;
    }

    public void map(MetricData metricData, Consumer<TelemetryItem> consumer) {
        if (EXCLUDED_METRIC_NAMES.contains(metricData.getName())) {
            return;
        }

        MetricDataType type = metricData.getType();
        if (type == DOUBLE_SUM
            || type == DOUBLE_GAUGE
            || type == LONG_SUM
            || type == LONG_GAUGE
            || type == HISTOGRAM) {
            boolean isPreAggregatedStandardMetric =
                OTEL_PRE_AGGREGATED_STANDARD_METRIC_NAMES.contains(metricData.getName());
            List<TelemetryItem> telemetryItemList =
                convertOtelMetricToAzureMonitorMetric(metricData, isPreAggregatedStandardMetric);
            for (TelemetryItem telemetryItem : telemetryItemList) {
                consumer.accept(telemetryItem);
            }
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
                pointDataValue = histogramPointData.getSum();
                pointBuilder.setMin(histogramPointData.getMin());
                pointBuilder.setMax(histogramPointData.getMax());
                break;
            case SUMMARY: // not supported yet in OpenTelemetry SDK
            case EXPONENTIAL_HISTOGRAM: // not supported yet in OpenTelemetry SDK
            default:
                throw new IllegalArgumentException("metric data type '" + type + "' is not supported yet");
        }

        pointBuilder.setValue(pointDataValue);
        // TODO (heya) why give it the same name as otel metric?
        //  it seems this field doesn't matter and only _MS.MetricId property matters?
        pointBuilder.setName(metricData.getName());
        metricTelemetryBuilder.setMetricPoint(pointBuilder);

        Attributes attributes = pointData.getAttributes();
        if (isPreAggregatedStandardMetric) {
            Long statusCode = attributes.get(SemanticAttributes.HTTP_STATUS_CODE);
            boolean success = isSuccess(statusCode, captureHttpServer4xxAsError);
            Boolean isSynthetic = attributes.get(IS_SYNTHETIC);

            attributes.forEach(
                (key, value) ->
                    applyConnectionStringAndRoleNameOverrides(
                        metricTelemetryBuilder, value, key.getKey()));

            if (metricData.getName().contains(".server.")) {
                RequestExtractor.extract(metricTelemetryBuilder, statusCode, success, isSynthetic);
            } else if (metricData.getName().contains(".client.")) {
                String dependencyType;
                int defaultPort;
                if (metricData.getName().startsWith("http")) {
                    dependencyType = "Http";
                    defaultPort = getDefaultPortForHttpScheme(attributes.get(SemanticAttributes.HTTP_SCHEME));
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
            MAPPINGS.map(attributes, metricTelemetryBuilder);
        }
    }

    static boolean applyConnectionStringAndRoleNameOverrides(
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

    private static boolean isSuccess(Long statusCode, boolean captureHttpServer4xxAsError) {
        if (captureHttpServer4xxAsError) {
            return statusCode == null || statusCode < 400;
        }
        return statusCode == null || statusCode < 500;
    }
}
