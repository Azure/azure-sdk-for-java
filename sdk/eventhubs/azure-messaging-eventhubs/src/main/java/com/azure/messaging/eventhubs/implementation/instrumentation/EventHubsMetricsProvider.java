// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.Meter;
import com.azure.messaging.eventhubs.models.Checkpoint;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.ERROR_TYPE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_EVENTHUBS_CONSUMER_GROUP;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_EVENTHUBS_CONSUMER_LAG;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_EVENTHUBS_DESTINATION_PARTITION_ID;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_PROCESS_DURATION;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_PROCESS_MESSAGES;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_PUBLISH_DURATION;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_PUBLISH_MESSAGES;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_RECEIVE_DURATION;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_RECEIVE_MESSAGES;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SETTLE_DURATION;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SYSTEM;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SYSTEM_VALUE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.SERVER_ADDRESS;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.getDurationInSeconds;

public class EventHubsMetricsProvider {
    private final Meter meter;
    private final boolean isEnabled;
    private Map<String, Object> commonAttributes;
    private AttributeCache attributeCacheSuccess;
    private LongCounter sendEventCounter;
    private LongCounter processEventCounter;
    private LongCounter receiveEventCounter;
    private DoubleHistogram sendBatchDuration;
    private DoubleHistogram processBatchDuration;
    private DoubleHistogram receiveBatchDuration;
    private DoubleHistogram consumerLag;
    private DoubleHistogram checkpointDuration;
    public EventHubsMetricsProvider(Meter meter, String namespace, String entityName, String consumerGroup) {
        this.meter = meter;
        this.isEnabled = meter != null && meter.isEnabled();
        if (this.isEnabled) {
            this.commonAttributes = getCommonAttributes(namespace, entityName, consumerGroup);
            this.attributeCacheSuccess = new AttributeCache(MESSAGING_EVENTHUBS_DESTINATION_PARTITION_ID, commonAttributes);

            this.sendEventCounter = meter.createLongCounter(MESSAGING_PUBLISH_MESSAGES, "Number of sent events", "{event}");
            this.sendBatchDuration = meter.createDoubleHistogram(MESSAGING_PUBLISH_DURATION, "Duration of publish operation including all retries", "s");

            this.processEventCounter = meter.createLongCounter(MESSAGING_PROCESS_MESSAGES, "Number of processed events", "{event}");
            this.processBatchDuration = meter.createDoubleHistogram(MESSAGING_PROCESS_DURATION, "Duration of processing callback", "s");

            this.receiveEventCounter = meter.createLongCounter(MESSAGING_RECEIVE_MESSAGES, "Number of received events", "{event}");
            this.receiveBatchDuration = meter.createDoubleHistogram(MESSAGING_RECEIVE_DURATION, "Duration of synchronous receive.", "s");
            this.checkpointDuration = meter.createDoubleHistogram(MESSAGING_SETTLE_DURATION, "Duration of checkpoint call", "ss");

            this.consumerLag = meter.createDoubleHistogram(MESSAGING_EVENTHUBS_CONSUMER_LAG, "Difference between local time when event was received and the local time it was enqueued on broker", "s");
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void reportBatchSend(int batchSize, String partitionId, InstrumentationScope scope) {
        if (isEnabled && (sendEventCounter.isEnabled() || sendBatchDuration.isEnabled())) {
            TelemetryAttributes attributes = getOrCreateAttributes(partitionId, scope.getErrorType());
            sendEventCounter.add(batchSize, attributes, scope.getSpan());
            sendBatchDuration.record(getDurationInSeconds(scope.getStartTime()), attributes, scope.getSpan());
        }
    }

    public void reportProcess(int batchSize, String partitionId, InstrumentationScope scope) {
        if (isEnabled && (processEventCounter.isEnabled() || processBatchDuration.isEnabled())) {
            TelemetryAttributes attributes = getOrCreateAttributes(partitionId, scope.getErrorType());
            processEventCounter.add(batchSize, attributes, scope.getSpan());
            processBatchDuration.record(getDurationInSeconds(scope.getStartTime()), attributes, scope.getSpan());
        }
    }

    public void reportReceiveDuration(int receivedCount, String partitionId, InstrumentationScope scope) {
        if (isEnabled && (receiveBatchDuration.isEnabled() || receiveEventCounter.isEnabled())) {
            String errorType = scope.getErrorType();
            TelemetryAttributes attributes = getOrCreateAttributes(partitionId, errorType);
            if (receivedCount > 0) {
                receiveEventCounter.add(receivedCount,
                        errorType == null ? attributes : getOrCreateAttributes(partitionId, null),
                        scope.getSpan());
            }

            receiveBatchDuration.record(getDurationInSeconds(scope.getStartTime()), attributes, scope.getSpan());
        }
    }

    public void reportLag(Instant enqueuedTime, String partitionId, InstrumentationScope scope) {
        if (isEnabled && consumerLag.isEnabled()) {
            consumerLag.record(getDurationInSeconds(enqueuedTime), attributeCacheSuccess.getOrCreate(partitionId), scope.getSpan());
        }
    }

    public void reportCheckpoint(Checkpoint checkpoint, InstrumentationScope scope) {
        if (isEnabled && checkpointDuration.isEnabled()) {
            checkpointDuration.record(getDurationInSeconds(scope.getStartTime()),
                    getOrCreateAttributes(checkpoint.getPartitionId(), scope.getErrorType()), scope.getSpan());
        }
    }

    private TelemetryAttributes getOrCreateAttributes(String partitionId, String errorType) {
        if (errorType == null) {
            return attributeCacheSuccess.getOrCreate(partitionId);
        }

        // we can potentially cache failure attributes, but the assumption is that
        // the cost of creating attributes is negligible comparing to throwing an exception
        Map<String, Object> attributes = new HashMap<>(commonAttributes);
        if (partitionId != null) {
            attributes.put(MESSAGING_EVENTHUBS_DESTINATION_PARTITION_ID, partitionId);
        }
        attributes.put(ERROR_TYPE, errorType);
        return meter.createAttributes(attributes);
    }

    private Map<String, Object> getCommonAttributes(String namespace, String entityName, String consumerGroup) {
        Map<String, Object> commonAttributesMap = new HashMap<>(3);
        commonAttributesMap.put(MESSAGING_SYSTEM, MESSAGING_SYSTEM_VALUE);
        commonAttributesMap.put(SERVER_ADDRESS, namespace);
        commonAttributesMap.put(MESSAGING_DESTINATION_NAME, entityName);
        if (consumerGroup != null) {
            commonAttributesMap.put(MESSAGING_EVENTHUBS_CONSUMER_GROUP, consumerGroup);
        }

        return Collections.unmodifiableMap(commonAttributesMap);
    }

    class AttributeCache {
        private final Map<String, TelemetryAttributes> attr = new ConcurrentHashMap<>();
        private final TelemetryAttributes commonAttr;
        private final Map<String, Object> commonMap;
        private final String dimensionName;

        AttributeCache(String dimensionName, Map<String, Object> common) {
            this.dimensionName = dimensionName;
            this.commonMap = common;
            this.commonAttr = meter.createAttributes(commonMap);
        }

        public TelemetryAttributes getOrCreate(String value) {
            if (value == null) {
                return commonAttr;
            }

            return attr.computeIfAbsent(value, this::create);
        }

        private TelemetryAttributes create(String value) {
            Map<String, Object> attributes = new HashMap<>(commonMap);
            attributes.put(dimensionName, value);
            return meter.createAttributes(attributes);
        }
    }
}
