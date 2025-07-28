// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.metrics.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.monitor.query.metrics.MetricsQueryServiceVersion;

public final class MonitorQueryMetricsUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    MonitorQueryMetricsUtils() {
    }

    public static MonitorQueryMetricsClientImpl getMetricsClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter, String endpoint,
        MetricsQueryServiceVersion serviceVersion) {
        return new MonitorQueryMetricsClientImpl(httpPipeline, serializerAdapter, endpoint, serviceVersion);
    }
}