// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import reactor.util.annotation.Nullable;

import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.FALSE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.MS_METRIC_ID;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.TRUE;
import static com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics.ExtractorHelper.extractCommon;

public final class RequestExtractor {

    // visible for testing
    public static final String REQUESTS_DURATION = "requests/duration";
    public static final String REQUEST_RESULT_CODE = "request/resultCode";
    public static final String REQUEST_SUCCESS = "Request.Success";

    public static void extract(MetricTelemetryBuilder metricBuilder, @Nullable Long statusCode, boolean success,
        @Nullable Boolean isSynthetic) {
        extractCommon(metricBuilder, isSynthetic);

        metricBuilder.addProperty(MS_METRIC_ID, REQUESTS_DURATION);
        if (statusCode != null) {
            metricBuilder.addProperty(REQUEST_RESULT_CODE, String.valueOf(statusCode));
        }
        metricBuilder.addProperty(REQUEST_SUCCESS, success ? TRUE : FALSE);
    }

    private RequestExtractor() {
    }
}
