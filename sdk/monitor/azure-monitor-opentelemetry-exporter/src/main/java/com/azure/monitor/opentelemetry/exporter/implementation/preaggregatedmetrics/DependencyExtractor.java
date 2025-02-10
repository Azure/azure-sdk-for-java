// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import reactor.util.annotation.Nullable;

import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.FALSE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.MS_METRIC_ID;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.TRUE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.extractCommon;

public final class DependencyExtractor {

    // visible for testing
    public static final String DEPENDENCIES_DURATION = "dependencies/duration";
    public static final String DEPENDENCY_TYPE = "Dependency.Type";
    public static final String DEPENDENCY_SUCCESS = "Dependency.Success";
    public static final String DEPENDENCY_TARGET = "dependency/target";
    public static final String DEPENDENCY_RESULT_CODE = "dependency/resultCode";

    public static void extract(MetricTelemetryBuilder metricBuilder, @Nullable Long statusCode, boolean success,
        String type, String target, @Nullable Boolean isSynthetic) {
        extractCommon(metricBuilder, isSynthetic);

        metricBuilder.addProperty(MS_METRIC_ID, DEPENDENCIES_DURATION);
        // TODO OTEL will provide rpc.grpc.status_code & rpc.success, http.success
        if (statusCode != null) {
            metricBuilder.addProperty(DEPENDENCY_RESULT_CODE, String.valueOf(statusCode));
        }
        metricBuilder.addProperty(DEPENDENCY_SUCCESS, success ? TRUE : FALSE);
        metricBuilder.addProperty(DEPENDENCY_TYPE, type);
        metricBuilder.addProperty(DEPENDENCY_TARGET, target);
    }

    private DependencyExtractor() {
    }
}
