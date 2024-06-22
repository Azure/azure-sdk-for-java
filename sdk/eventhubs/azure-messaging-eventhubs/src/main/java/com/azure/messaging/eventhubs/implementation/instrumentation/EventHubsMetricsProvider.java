// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.logging.ClientLogger;
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
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_CLIENT_CONSUMED_MESSAGES;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_CLIENT_OPERATION_DURATION;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_CLIENT_PUBLISHED_MESSAGES;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_EVENTHUBS_CONSUMER_LAG;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_PARTITION_ID;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_OPERATION_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_OPERATION_TYPE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_PROCESS_DURATION;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SYSTEM;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SYSTEM_VALUE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.SERVER_ADDRESS;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.getDurationInSeconds;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.getOperationType;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.CHECKPOINT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.PROCESS;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.RECEIVE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.SEND;

public class EventHubsMetricsProvider {
    private final Meter meter;
    private final boolean isEnabled;
    private static final ClientLogger LOGGER = new ClientLogger(EventHubsMetricsProvider.class);
    private Map<String, Object> commonAttributes;
    private AttributeCache sendAttributeCacheSuccess;
    private AttributeCache receiveAttributeCacheSuccess;
    private AttributeCache checkpointAttributeCacheSuccess;
    private AttributeCache processAttributeCacheSuccess;
    private AttributeCache lagAttributeCache;
    private LongCounter publishedEventCounter;
    private LongCounter consumedEventCounter;
    private DoubleHistogram operationDuration;
    private DoubleHistogram processDuration;
    private DoubleHistogram consumerLag;
    public EventHubsMetricsProvider(Meter meter, String namespace, String entityName, String consumerGroup) {
        this.meter = meter;
        this.isEnabled = meter != null && meter.isEnabled();
        if (this.isEnabled) {
            this.commonAttributes = getCommonAttributes(namespace, entityName, consumerGroup);
            this.sendAttributeCacheSuccess = AttributeCache.create(meter, SEND, commonAttributes);
            this.receiveAttributeCacheSuccess = AttributeCache.create(meter, RECEIVE, commonAttributes);
            this.checkpointAttributeCacheSuccess = AttributeCache.create(meter, CHECKPOINT, commonAttributes);
            this.processAttributeCacheSuccess = AttributeCache.create(meter, PROCESS, commonAttributes);
            this.lagAttributeCache = new AttributeCache(meter, MESSAGING_DESTINATION_PARTITION_ID, commonAttributes);

            this.publishedEventCounter = meter.createLongCounter(MESSAGING_CLIENT_PUBLISHED_MESSAGES, "The number of published events", "{event}");
            this.consumedEventCounter = meter.createLongCounter(MESSAGING_CLIENT_CONSUMED_MESSAGES, "The number of consumed events", "{event}");

            this.operationDuration = meter.createDoubleHistogram(MESSAGING_CLIENT_OPERATION_DURATION, "The duration of client messaging operations involving communication with the Event Hubs namespace", "s");
            this.processDuration = meter.createDoubleHistogram(MESSAGING_PROCESS_DURATION, "The duration of the processing callback", "s");

            this.consumerLag = meter.createDoubleHistogram(MESSAGING_EVENTHUBS_CONSUMER_LAG, "Difference between local time when event was received and the local time it was enqueued on broker", "s");
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void reportBatchSend(int batchSize, String partitionId, InstrumentationScope scope) {
        if (isEnabled && (publishedEventCounter.isEnabled() || operationDuration.isEnabled())) {
            TelemetryAttributes attributes = getOrCreateAttributes(SEND, partitionId, scope.getErrorType());
            publishedEventCounter.add(batchSize, attributes, scope.getSpan());
            operationDuration.record(getDurationInSeconds(scope.getStartTime()), attributes, scope.getSpan());
        }
    }

    public void reportProcess(int batchSize, String partitionId, InstrumentationScope scope) {
        if (isEnabled && (consumedEventCounter.isEnabled() || processDuration.isEnabled())) {
            TelemetryAttributes attributes = getOrCreateAttributes(PROCESS, partitionId, scope.getErrorType());
            consumedEventCounter.add(batchSize, attributes, scope.getSpan());
            processDuration.record(getDurationInSeconds(scope.getStartTime()), attributes, scope.getSpan());
        }
    }

    public void reportReceive(int receivedCount, String partitionId, InstrumentationScope scope) {
        if (isEnabled && (operationDuration.isEnabled() || consumedEventCounter.isEnabled())) {
            String errorType = scope.getErrorType();
            TelemetryAttributes attributes = getOrCreateAttributes(RECEIVE, partitionId, errorType);
            if (receivedCount > 0) {
                consumedEventCounter.add(receivedCount,
                        errorType == null ? attributes : getOrCreateAttributes(RECEIVE, partitionId, null),
                        scope.getSpan());
            }

            operationDuration.record(getDurationInSeconds(scope.getStartTime()), attributes, scope.getSpan());
        }
    }

    public void reportLag(Instant enqueuedTime, String partitionId, InstrumentationScope scope) {
        if (isEnabled && consumerLag.isEnabled()) {
            consumerLag.record(getDurationInSeconds(enqueuedTime), lagAttributeCache.getOrCreate(partitionId), scope.getSpan());
        }
    }

    public void reportCheckpoint(Checkpoint checkpoint, InstrumentationScope scope) {
        if (isEnabled && operationDuration.isEnabled()) {
            operationDuration.record(getDurationInSeconds(scope.getStartTime()),
                    getOrCreateAttributes(CHECKPOINT, checkpoint.getPartitionId(), scope.getErrorType()), scope.getSpan());
        }
    }

    private TelemetryAttributes getOrCreateAttributes(OperationName operationName, String partitionId, String errorType) {
        if (errorType == null) {
            switch (operationName) {
                case SEND:
                    return sendAttributeCacheSuccess.getOrCreate(partitionId);
                case RECEIVE:
                    return receiveAttributeCacheSuccess.getOrCreate(partitionId);
                case CHECKPOINT:
                    return checkpointAttributeCacheSuccess.getOrCreate(partitionId);
                case PROCESS:
                    return processAttributeCacheSuccess.getOrCreate(partitionId);
                default:
                    LOGGER.atVerbose()
                        .addKeyValue("operationName", operationName)
                        .log("Unknown operation name");
                    // this should never happen
                    return lagAttributeCache.getOrCreate(partitionId);
            }
        }

        // we can potentially cache failure attributes, but the assumption is that
        // the cost of creating attributes is negligible comparing to throwing an exception
        Map<String, Object> attributes = new HashMap<>(commonAttributes);
        if (partitionId != null) {
            attributes.put(MESSAGING_DESTINATION_PARTITION_ID, partitionId);
        }

        setOperation(attributes, operationName);
        attributes.put(ERROR_TYPE, errorType);
        return meter.createAttributes(attributes);
    }

    private Map<String, Object> getCommonAttributes(String namespace, String entityName, String consumerGroup) {
        Map<String, Object> commonAttributesMap = new HashMap<>(3);
        commonAttributesMap.put(MESSAGING_SYSTEM, MESSAGING_SYSTEM_VALUE);
        commonAttributesMap.put(SERVER_ADDRESS, namespace);
        commonAttributesMap.put(MESSAGING_DESTINATION_NAME, entityName);
        if (consumerGroup != null) {
            commonAttributesMap.put(MESSAGING_CONSUMER_GROUP_NAME, consumerGroup);
        }

        return Collections.unmodifiableMap(commonAttributesMap);
    }

    private static void setOperation(Map<String, Object> attributes, OperationName name) {
        String operationType = getOperationType(name);
        if (operationType != null) {
            attributes.put(MESSAGING_OPERATION_TYPE, operationType);
        }

        attributes.put(MESSAGING_OPERATION_NAME, name.toString());
    }

    private static final class AttributeCache {
        private final Map<String, TelemetryAttributes> attr = new ConcurrentHashMap<>();
        private final TelemetryAttributes commonAttr;
        private final Map<String, Object> commonMap;
        private final String dimensionName;
        private final Meter meter;

        static AttributeCache create(Meter meter, OperationName operationName, Map<String, Object> commonAttributes) {
            Map<String, Object> attributes = new HashMap<>(commonAttributes);
            setOperation(attributes, operationName);
            return new AttributeCache(meter, MESSAGING_DESTINATION_PARTITION_ID, attributes);
        }

        private AttributeCache(Meter meter, String dimensionName, Map<String, Object> common) {
            this.dimensionName = dimensionName;
            this.commonMap = common;
            this.meter = meter;
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
