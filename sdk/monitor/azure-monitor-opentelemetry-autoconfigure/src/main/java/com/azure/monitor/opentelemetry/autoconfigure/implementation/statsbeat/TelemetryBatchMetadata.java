// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of per-telemetry-type item counts for a batch of TelemetryItems.
 * Used to propagate customer-facing SDKStats metadata through the telemetry pipeline
 * without exposing three separate map parameters.
 */
public final class TelemetryBatchMetadata {

    private static final TelemetryBatchMetadata EMPTY
        = new TelemetryBatchMetadata(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

    private final Map<String, Long> itemCountsByType;
    private final Map<String, Long> successItemCountsByType;
    private final Map<String, Long> failureItemCountsByType;

    TelemetryBatchMetadata(Map<String, Long> itemCountsByType, Map<String, Long> successItemCountsByType,
        Map<String, Long> failureItemCountsByType) {
        this.itemCountsByType = itemCountsByType;
        this.successItemCountsByType = successItemCountsByType;
        this.failureItemCountsByType = failureItemCountsByType;
    }

    /**
     * Returns a shared empty instance (no item counts). Used for batches where
     * item counting is not applicable (e.g. statsbeat, disk retries, SDKStats metrics).
     */
    public static TelemetryBatchMetadata empty() {
        return EMPTY;
    }

    /**
     * Computes per-type item counts from a list of TelemetryItems.
     * Internal items (e.g. Statsbeat) are excluded from the counts.
     * For REQUEST and DEPENDENCY types, success/failure is split based on isSuccess().
     */
    public static TelemetryBatchMetadata fromTelemetryItems(List<TelemetryItem> telemetryItems) {
        Map<String, Long> itemCountsByType = new HashMap<>();
        Map<String, Long> successItemCountsByType = new HashMap<>();
        Map<String, Long> failureItemCountsByType = new HashMap<>();

        for (TelemetryItem item : telemetryItems) {
            String telemetryType = CustomerSdkStatsTelemetryType.fromTelemetryItemName(item.getName());
            if (telemetryType == null) {
                // Skip internal items (e.g. Statsbeat)
                continue;
            }

            itemCountsByType.merge(telemetryType, 1L, Long::sum);

            // Track success/failure for Request and Dependency types
            if ("REQUEST".equals(telemetryType) || "DEPENDENCY".equals(telemetryType)) {
                MonitorDomain baseData = item.getData() != null ? item.getData().getBaseData() : null;
                Boolean success = null;
                if (baseData instanceof RequestData) {
                    success = ((RequestData) baseData).isSuccess();
                } else if (baseData instanceof RemoteDependencyData) {
                    success = ((RemoteDependencyData) baseData).isSuccess();
                }
                if (success != null && success) {
                    successItemCountsByType.merge(telemetryType, 1L, Long::sum);
                } else if (success != null) {
                    // Only count explicit false as failure; null is treated as unknown
                    failureItemCountsByType.merge(telemetryType, 1L, Long::sum);
                }
            }
        }

        if (itemCountsByType.isEmpty()) {
            return EMPTY;
        }

        return new TelemetryBatchMetadata(Collections.unmodifiableMap(itemCountsByType),
            Collections.unmodifiableMap(successItemCountsByType), Collections.unmodifiableMap(failureItemCountsByType));
    }

    /**
     * Returns item counts by telemetry type (e.g. "REQUEST" -> 200, "DEPENDENCY" -> 300).
     * Empty map for batches where item counting is not applicable.
     */
    public Map<String, Long> getItemCountsByType() {
        return itemCountsByType;
    }

    /**
     * Returns counts of successful REQUEST/DEPENDENCY items (where isSuccess() == true).
     */
    public Map<String, Long> getSuccessItemCountsByType() {
        return successItemCountsByType;
    }

    /**
     * Returns counts of failed REQUEST/DEPENDENCY items (where isSuccess() == false).
     */
    public Map<String, Long> getFailureItemCountsByType() {
        return failureItemCountsByType;
    }

    /**
     * Returns true if this metadata has no item counts.
     */
    public boolean isEmpty() {
        return itemCountsByType.isEmpty();
    }
}
