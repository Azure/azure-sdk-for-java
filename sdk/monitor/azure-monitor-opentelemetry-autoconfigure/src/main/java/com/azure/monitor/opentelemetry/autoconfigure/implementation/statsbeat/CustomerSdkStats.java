// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedTime;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Accumulates customer-facing SDKStats metrics: Item_Success_Count, Item_Dropped_Count,
 * Item_Retry_Count. These metrics are sent to the customer's own Application Insights resource.
 */
public class CustomerSdkStats {

    static final String ITEM_SUCCESS_COUNT = "Item_Success_Count";
    static final String ITEM_DROPPED_COUNT = "Item_Dropped_Count";
    static final String ITEM_RETRY_COUNT = "Item_Retry_Count";

    // Drop code constants
    public static final String DROP_CODE_CLIENT_EXCEPTION = "CLIENT_EXCEPTION";
    public static final String DROP_CODE_CLIENT_READONLY = "CLIENT_READONLY";
    public static final String DROP_CODE_CLIENT_PERSISTENCE_CAPACITY = "CLIENT_PERSISTENCE_CAPACITY";
    public static final String DROP_CODE_CLIENT_STORAGE_DISABLED = "CLIENT_STORAGE_DISABLED";

    // Retry code constants
    public static final String RETRY_CODE_CLIENT_EXCEPTION = "CLIENT_EXCEPTION";
    public static final String RETRY_CODE_CLIENT_TIMEOUT = "CLIENT_TIMEOUT";

    private final ConcurrentHashMap<SuccessKey, AtomicLong> successCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<DroppedKey, AtomicLong> droppedCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<RetryKey, AtomicLong> retryCounts = new ConcurrentHashMap<>();

    private final String computeType;
    private final String language;
    private final String version;

    /**
     * Creates a CustomerSdkStats instance using the detected resource provider for compute type.
     */
    public static CustomerSdkStats create(String version) {
        String computeType = ResourceProvider.initResourceProvider().getValue();
        return new CustomerSdkStats(computeType, "java", version);
    }

    // visible for testing
    public CustomerSdkStats(String computeType, String language, String version) {
        this.computeType = computeType;
        this.language = language;
        this.version = version;
    }

    /**
     * Increment success counts for all telemetry types in the batch.
     */
    public void incrementSuccessCount(Map<String, Long> itemCountsByType) {
        for (Map.Entry<String, Long> entry : itemCountsByType.entrySet()) {
            SuccessKey key = new SuccessKey(entry.getKey());
            successCounts.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(entry.getValue());
        }
    }

    /**
     * Increment dropped counts for all telemetry types in the batch.
     *
     * @param itemCountsByType the per-type item counts
     * @param dropCode the drop code (e.g. "CLIENT_EXCEPTION", "402", etc.)
     * @param dropReason the drop reason (e.g. "Exceeded daily quota")
     * @param successItemCountsByType success items by type (for telemetry_success dimension on
     *     REQUEST/DEPENDENCY)
     * @param failureItemCountsByType failure items by type
     */
    public void incrementDroppedCount(Map<String, Long> itemCountsByType, String dropCode, @Nullable String dropReason,
        Map<String, Long> successItemCountsByType, Map<String, Long> failureItemCountsByType) {
        for (Map.Entry<String, Long> entry : itemCountsByType.entrySet()) {
            String telemetryType = entry.getKey();
            long totalCount = entry.getValue();

            // For REQUEST and DEPENDENCY, split by telemetry_success
            if ("REQUEST".equals(telemetryType) || "DEPENDENCY".equals(telemetryType)) {
                long successCount = successItemCountsByType.getOrDefault(telemetryType, 0L);
                long failureCount = failureItemCountsByType.getOrDefault(telemetryType, 0L);
                // Any items not accounted for in success/failure maps go to the "unknown" bucket
                long unaccounted = Math.max(0, totalCount - successCount - failureCount);

                if (successCount > 0) {
                    DroppedKey key = new DroppedKey(telemetryType, dropCode, dropReason, Boolean.TRUE);
                    droppedCounts.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(successCount);
                }
                if (failureCount > 0) {
                    DroppedKey key = new DroppedKey(telemetryType, dropCode, dropReason, Boolean.FALSE);
                    droppedCounts.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(failureCount);
                }
                if (unaccounted > 0) {
                    // Items where we couldn't determine success/failure
                    DroppedKey key = new DroppedKey(telemetryType, dropCode, dropReason, null);
                    droppedCounts.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(unaccounted);
                }
            } else {
                // For non-REQUEST/DEPENDENCY types, telemetry_success is not applicable
                DroppedKey key = new DroppedKey(telemetryType, dropCode, dropReason, null);
                droppedCounts.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(totalCount);
            }
        }
    }

    /**
     * Increment retry counts for all telemetry types in the batch.
     */
    public void incrementRetryCount(Map<String, Long> itemCountsByType, String retryCode,
        @Nullable String retryReason) {
        for (Map.Entry<String, Long> entry : itemCountsByType.entrySet()) {
            RetryKey key = new RetryKey(entry.getKey(), retryCode, retryReason);
            retryCounts.computeIfAbsent(key, k -> new AtomicLong()).addAndGet(entry.getValue());
        }
    }

    /**
     * Atomically snapshots and clears all counters, returning a list of TelemetryItem metrics
     * to send to the customer's AI resource.
     *
     * @param connectionString the customer's connection string
     * @param sdkVersion the full SDK version string for ai.internal.sdkVersion tag
     * @param cloudRole the cloud role name (nullable)
     * @param cloudRoleInstance the cloud role instance (nullable)
     * @return list of TelemetryItem metrics, empty if no non-zero counters
     */
    public List<TelemetryItem> collectAndReset(ConnectionString connectionString, String sdkVersion,
        @Nullable String cloudRole, @Nullable String cloudRoleInstance) {
        List<TelemetryItem> telemetryItems = new ArrayList<>();

        // Snapshot and clear success counts
        Map<SuccessKey, Long> successSnapshot = snapshotAndClear(successCounts);
        for (Map.Entry<SuccessKey, Long> entry : successSnapshot.entrySet()) {
            long count = entry.getValue();
            if (count == 0) {
                continue;
            }
            SuccessKey key = entry.getKey();
            MetricTelemetryBuilder builder = MetricTelemetryBuilder.create(ITEM_SUCCESS_COUNT, (double) count);
            builder.setConnectionString(connectionString);
            builder.setTime(FormattedTime.offSetDateTimeFromNow());
            addCommonTags(builder, sdkVersion, cloudRole, cloudRoleInstance);
            addCommonProperties(builder);
            builder.addProperty("telemetry_type", key.telemetryType);
            telemetryItems.add(builder.build());
        }

        // Snapshot and clear dropped counts
        Map<DroppedKey, Long> droppedSnapshot = snapshotAndClear(droppedCounts);
        for (Map.Entry<DroppedKey, Long> entry : droppedSnapshot.entrySet()) {
            long count = entry.getValue();
            if (count == 0) {
                continue;
            }
            DroppedKey key = entry.getKey();
            MetricTelemetryBuilder builder = MetricTelemetryBuilder.create(ITEM_DROPPED_COUNT, (double) count);
            builder.setConnectionString(connectionString);
            builder.setTime(FormattedTime.offSetDateTimeFromNow());
            addCommonTags(builder, sdkVersion, cloudRole, cloudRoleInstance);
            addCommonProperties(builder);
            builder.addProperty("telemetry_type", key.telemetryType);
            builder.addProperty("drop.code", key.dropCode);
            if (key.dropReason != null) {
                builder.addProperty("drop.reason", key.dropReason);
            }
            if (key.telemetrySuccess != null) {
                builder.addProperty("telemetry_success", key.telemetrySuccess.toString());
            }
            telemetryItems.add(builder.build());
        }

        // Snapshot and clear retry counts
        Map<RetryKey, Long> retrySnapshot = snapshotAndClear(retryCounts);
        for (Map.Entry<RetryKey, Long> entry : retrySnapshot.entrySet()) {
            long count = entry.getValue();
            if (count == 0) {
                continue;
            }
            RetryKey key = entry.getKey();
            MetricTelemetryBuilder builder = MetricTelemetryBuilder.create(ITEM_RETRY_COUNT, (double) count);
            builder.setConnectionString(connectionString);
            builder.setTime(FormattedTime.offSetDateTimeFromNow());
            addCommonTags(builder, sdkVersion, cloudRole, cloudRoleInstance);
            addCommonProperties(builder);
            builder.addProperty("telemetry_type", key.telemetryType);
            builder.addProperty("retry.code", key.retryCode);
            if (key.retryReason != null) {
                builder.addProperty("retry.reason", key.retryReason);
            }
            telemetryItems.add(builder.build());
        }

        return telemetryItems;
    }

    private void addCommonTags(MetricTelemetryBuilder builder, String sdkVersion, @Nullable String cloudRole,
        @Nullable String cloudRoleInstance) {
        builder.addTag(ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), sdkVersion);
        if (cloudRole != null) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), cloudRole);
        }
        if (cloudRoleInstance != null) {
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), cloudRoleInstance);
        }
    }

    private void addCommonProperties(MetricTelemetryBuilder builder) {
        builder.addProperty("computeType", computeType);
        builder.addProperty("language", language);
        builder.addProperty("version", version);
    }

    private static <K> Map<K, Long> snapshotAndClear(ConcurrentHashMap<K, AtomicLong> map) {
        Map<K, Long> snapshot = new HashMap<>();
        // We iterate and getAndSet(0) to avoid losing concurrent increments
        for (Map.Entry<K, AtomicLong> entry : map.entrySet()) {
            long value = entry.getValue().getAndSet(0);
            if (value != 0) {
                snapshot.put(entry.getKey(), value);
            }
        }
        // Note: we don't remove zero-valued entries here because doing so with removeIf()
        // can race with concurrent increment*() calls, causing those increments to be lost.
        // The number of distinct keys is bounded by the set of telemetry types and code/reason
        // combinations, so unbounded growth is not a concern in practice.
        return snapshot;
    }

    // visible for testing
    long getSuccessCount(String telemetryType) {
        AtomicLong count = successCounts.get(new SuccessKey(telemetryType));
        return count == null ? 0L : count.get();
    }

    // visible for testing
    long getDroppedCount(String telemetryType, String dropCode) {
        long total = 0;
        for (Map.Entry<DroppedKey, AtomicLong> entry : droppedCounts.entrySet()) {
            DroppedKey key = entry.getKey();
            if (key.telemetryType.equals(telemetryType) && key.dropCode.equals(dropCode)) {
                total += entry.getValue().get();
            }
        }
        return total;
    }

    // visible for testing
    long getRetryCount(String telemetryType, String retryCode) {
        long total = 0;
        for (Map.Entry<RetryKey, AtomicLong> entry : retryCounts.entrySet()) {
            RetryKey key = entry.getKey();
            if (key.telemetryType.equals(telemetryType) && key.retryCode.equals(retryCode)) {
                total += entry.getValue().get();
            }
        }
        return total;
    }

    static final class SuccessKey {
        final String telemetryType;

        SuccessKey(String telemetryType) {
            this.telemetryType = telemetryType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SuccessKey)) {
                return false;
            }
            SuccessKey that = (SuccessKey) o;
            return telemetryType.equals(that.telemetryType);
        }

        @Override
        public int hashCode() {
            return telemetryType.hashCode();
        }
    }

    static final class DroppedKey {
        final String telemetryType;
        final String dropCode;
        final String dropReason;
        final Boolean telemetrySuccess;

        DroppedKey(String telemetryType, String dropCode, @Nullable String dropReason,
            @Nullable Boolean telemetrySuccess) {
            this.telemetryType = telemetryType;
            this.dropCode = dropCode;
            this.dropReason = dropReason;
            this.telemetrySuccess = telemetrySuccess;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DroppedKey)) {
                return false;
            }
            DroppedKey that = (DroppedKey) o;
            return telemetryType.equals(that.telemetryType)
                && dropCode.equals(that.dropCode)
                && Objects.equals(dropReason, that.dropReason)
                && Objects.equals(telemetrySuccess, that.telemetrySuccess);
        }

        @Override
        public int hashCode() {
            return Objects.hash(telemetryType, dropCode, dropReason, telemetrySuccess);
        }
    }

    static final class RetryKey {
        final String telemetryType;
        final String retryCode;
        final String retryReason;

        RetryKey(String telemetryType, String retryCode, @Nullable String retryReason) {
            this.telemetryType = telemetryType;
            this.retryCode = retryCode;
            this.retryReason = retryReason;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RetryKey)) {
                return false;
            }
            RetryKey that = (RetryKey) o;
            return telemetryType.equals(that.telemetryType)
                && retryCode.equals(that.retryCode)
                && Objects.equals(retryReason, that.retryReason);
        }

        @Override
        public int hashCode() {
            return Objects.hash(telemetryType, retryCode, retryReason);
        }
    }
}
