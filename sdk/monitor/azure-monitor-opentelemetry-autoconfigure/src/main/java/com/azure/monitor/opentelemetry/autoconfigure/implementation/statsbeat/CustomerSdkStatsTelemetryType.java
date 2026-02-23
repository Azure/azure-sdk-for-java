// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps TelemetryItem.getName() values to the telemetry_type dimension values
 * used in customer-facing SDKStats metrics.
 */
public final class CustomerSdkStatsTelemetryType {

    private static final Map<String, String> MAPPING = new HashMap<>();

    static {
        MAPPING.put("Request", "REQUEST");
        MAPPING.put("RemoteDependency", "DEPENDENCY");
        MAPPING.put("Message", "TRACE");
        MAPPING.put("Exception", "EXCEPTION");
        MAPPING.put("Metric", "CUSTOM_METRIC");
        MAPPING.put("Event", "CUSTOM_EVENT");
        MAPPING.put("PageView", "PAGE_VIEW");
        MAPPING.put("Availability", "AVAILABILITY");
    }

    /**
     * Maps a TelemetryItem name to its customer-facing telemetry_type value.
     *
     * @param telemetryItemName the value from TelemetryItem.getName()
     * @return the telemetry_type dimension value, or null if the item should be skipped
     *         (e.g. "Statsbeat" internal metrics)
     */
    @Nullable
    public static String fromTelemetryItemName(@Nullable String telemetryItemName) {
        if (telemetryItemName == null) {
            return null;
        }
        return MAPPING.get(telemetryItemName);
    }

    private CustomerSdkStatsTelemetryType() {
    }
}
