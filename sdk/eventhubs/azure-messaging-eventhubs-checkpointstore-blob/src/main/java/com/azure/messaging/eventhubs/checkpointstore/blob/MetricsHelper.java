// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.LongGauge;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import com.azure.messaging.eventhubs.models.Checkpoint;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

final class MetricsHelper {
    private static final ClientLogger LOGGER = new ClientLogger(MetricsHelper.class);

    // Make sure attribute names are consistent across AMQP Core, EventHubs, ServiceBus when applicable
    // and mapped correctly in OTel Metrics https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-metrics-opentelemetry/src/main/java/com/azure/core/metrics/opentelemetry/OpenTelemetryAttributes.java
    private static final String ENTITY_NAME_KEY = "entityName";
    private static final String HOSTNAME_KEY = "hostName";
    private static final String PARTITION_ID_KEY = "partitionId";
    private static final String CONSUMER_GROUP_KEY = "consumerGroup";
    private static final String STATUS_KEY = "status";

    // since checkpoint store is stateless it might be used for endless number of eventhubs.
    // we'll have as many subscriptions as there are combinations of fqdn, eventhub name, partitionId and consumer group.
    // In the unlikely case it's shared across a lot of EH client instances, metrics would be too costly
    // and unhelpful. So, let's just set a hard limit on number of subscriptions.
    private static final int MAX_ATTRIBUTES_SETS = 100;

    private static final String PROPERTIES_FILE = "azure-messaging-eventhubs-checkpointstore-blob.properties";
    private static final String NAME_KEY = "name";
    private static final String VERSION_KEY = "version";
    private static final String LIBRARY_NAME;
    private static final String LIBRARY_VERSION;
    private static final String UNKNOWN = "UNKNOWN";

    static {
        final Map<String, String> properties = CoreUtils.getProperties(PROPERTIES_FILE);
        LIBRARY_NAME = properties.getOrDefault(NAME_KEY, UNKNOWN);
        LIBRARY_VERSION = properties.getOrDefault(VERSION_KEY, UNKNOWN);
    }

    private final ConcurrentHashMap<String, TelemetryAttributes> common = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TelemetryAttributes> checkpointFailure = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TelemetryAttributes> checkpointSuccess = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CurrentValue> seqNoSubscriptions = new ConcurrentHashMap<>();

    private volatile boolean maxCapacityReached = false;

    private final Meter meter;
    private final LongGauge lastSequenceNumber;
    private final DoubleHistogram checkpointDuration;
    private final boolean isEnabled;

    MetricsHelper(MetricsOptions metricsOptions, MeterProvider meterProvider) {
        if (areMetricsEnabled(metricsOptions)) {
            this.meter = meterProvider.createMeter(LIBRARY_NAME, LIBRARY_VERSION, metricsOptions);
            this.isEnabled = this.meter.isEnabled();
        } else {
            this.meter = null;
            this.isEnabled = false;
        }

        if (isEnabled) {
            this.lastSequenceNumber = this.meter.createLongGauge("messaging.eventhubs.checkpoint.sequence_number", "Last successfully checkpointed sequence number.", "seqNo");
            this.checkpointDuration = this.meter.createDoubleHistogram("messaging.eventhubs.checkpoint.duration", "Duration of checkpoint call.", "ms");
        } else {
            this.lastSequenceNumber = null;
            this.checkpointDuration = null;
        }
    }

    boolean isCheckpointDurationEnabled() {
        return isEnabled && checkpointDuration.isEnabled();
    }

    void reportCheckpoint(Checkpoint checkpoint, String attributesId, boolean success, Instant startTime) {
        if (!isEnabled || !(lastSequenceNumber.isEnabled() && checkpointDuration.isEnabled())) {
            return;
        }

        if (!maxCapacityReached && (seqNoSubscriptions.size() >= MAX_ATTRIBUTES_SETS || common.size() >= MAX_ATTRIBUTES_SETS)) {
            LOGGER.error("Too many attribute combinations are reported for checkpoint metrics, ignoring any new dimensions.");
            maxCapacityReached = true;
        }

        if (lastSequenceNumber.isEnabled() && success) {
            updateCurrentValue(attributesId, checkpoint);
        }

        if (checkpointDuration.isEnabled()) {
            TelemetryAttributes attributes;
            if (success) {
                attributes = getOrCreate(checkpointSuccess, attributesId, checkpoint, null);
            } else {
                attributes = getOrCreate(checkpointFailure, attributesId, checkpoint, "error");
            }

            if (attributes != null) {
                if (checkpointDuration.isEnabled()) {
                    checkpointDuration.record(Duration.between(startTime, Instant.now()).toMillis(), attributes, Context.NONE);
                }
            }
        }
    }

    private TelemetryAttributes getOrCreate(ConcurrentHashMap<String, TelemetryAttributes> source, String attributesId, Checkpoint checkpoint, String status) {
        if (maxCapacityReached) {
            return source.get(attributesId);
        }

        return source.computeIfAbsent(attributesId, i -> meter.createAttributes(createAttributes(checkpoint, status)));
    }

    private Map<String, Object> createAttributes(Checkpoint checkpoint, String status) {
        Map<String, Object> attributesMap = new HashMap<>(5);
        attributesMap.put(HOSTNAME_KEY, checkpoint.getFullyQualifiedNamespace());
        attributesMap.put(ENTITY_NAME_KEY, checkpoint.getEventHubName());
        attributesMap.put(PARTITION_ID_KEY, checkpoint.getPartitionId());
        attributesMap.put(CONSUMER_GROUP_KEY, checkpoint.getConsumerGroup());
        if (status != null) {
            attributesMap.put(STATUS_KEY, status);
        }

        return attributesMap;
    }

    private void updateCurrentValue(String attributesId, Checkpoint checkpoint) {
        if (checkpoint.getSequenceNumber() == null) {
            return;
        }

        final CurrentValue valueSupplier;
        if (maxCapacityReached) {
            valueSupplier = seqNoSubscriptions.get(attributesId);
            if (valueSupplier == null) {
                return;
            }
        } else {
            TelemetryAttributes attributes = getOrCreate(common, attributesId, checkpoint, null);
            if (attributes == null) {
                return;
            }

            valueSupplier = seqNoSubscriptions.computeIfAbsent(attributesId, a -> {
                AtomicReference<Long> lastSeqNo = new AtomicReference<>();
                return new CurrentValue(lastSequenceNumber.registerCallback(() -> lastSeqNo.get(), attributes), lastSeqNo);
            });
        }

        valueSupplier.set(checkpoint.getSequenceNumber());
    }

    private static boolean areMetricsEnabled(MetricsOptions options) {
        if (options == null || options.isEnabled()) {
            return true;
        }

        return false;
    }

    private static class CurrentValue {
        private final AtomicReference<Long> lastSeqNo;
        private final AutoCloseable subscription;

        CurrentValue(AutoCloseable subscription, AtomicReference<Long> lastSeqNo) {
            this.subscription = subscription;
            this.lastSeqNo = lastSeqNo;
        }

        void set(long value) {
            lastSeqNo.set(value);
        }

        void close() {
            if (subscription != null) {
                try {
                    subscription.close();
                } catch (Exception e) {
                    // should never happen
                    throw LOGGER.logThrowableAsWarning(new RuntimeException(e));
                }
            }
        }
    }
}
