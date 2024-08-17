// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.preaggregatedmetrics;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import reactor.util.annotation.Nullable;

import java.util.Map;

public final class ExtractorHelper {

    // visible for testing
    public static final String MS_METRIC_ID = "_MS.MetricId";
    public static final String MS_IS_AUTOCOLLECTED = "_MS.IsAutocollected";
    public static final String TRUE = "True";
    public static final String FALSE = "False";
    public static final String OPERATION_SYNTHETIC = "operation/synthetic";
    public static final String CLOUD_ROLE_NAME = "cloud/roleName";
    public static final String CLOUD_ROLE_INSTANCE = "cloud/roleInstance";

    static void extractCommon(MetricTelemetryBuilder metricBuilder, @Nullable Boolean isSynthetic) {
        metricBuilder.addProperty(MS_IS_AUTOCOLLECTED, TRUE);
        Map<String, String> tags = metricBuilder.build().getTags();
        if (tags != null) {
            String cloudName = tags.get(ContextTagKeys.AI_CLOUD_ROLE.toString());
            if (cloudName != null && !cloudName.isEmpty()) {
                metricBuilder.addProperty(CLOUD_ROLE_NAME, cloudName);
            }

            String cloudRoleInstance = tags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString());
            if (cloudRoleInstance != null && !cloudRoleInstance.isEmpty()) {
                metricBuilder.addProperty(CLOUD_ROLE_INSTANCE, cloudRoleInstance);
            }
        }

        metricBuilder.addProperty(OPERATION_SYNTHETIC, isSynthetic != null && isSynthetic ? TRUE : FALSE);
    }

    private ExtractorHelper() {
    }
}
