// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.Meter;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.models.PartitionEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_NAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.HOSTNAME_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.CONSUMER_GROUP_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

public class EventHubsMetricsProvider {
    private static final String GENERIC_STATUS_KEY = "status";
    private final Meter meter;
    private final boolean isEnabled;

    private AttributeCache sendAttributeCacheSuccess;
    private AttributeCache sendAttributeCacheFailure;
    private AttributeCache receiveAttributeCache;
    private LongCounter sentEventsCounter;
    private DoubleHistogram consumerLag;

    public EventHubsMetricsProvider(Meter meter, String namespace, String entityName, String consumerGroup) {
        this.meter = meter;
        this.isEnabled = meter != null && meter.isEnabled();
        if (this.isEnabled) {
            Map<String, Object> commonAttributesMap = new HashMap<>(3);
            commonAttributesMap.put(HOSTNAME_KEY, namespace);
            commonAttributesMap.put(ENTITY_NAME_KEY, entityName);
            if (consumerGroup != null) {
                commonAttributesMap.put(CONSUMER_GROUP_KEY, consumerGroup);
            }

            Map<String, Object> successMap = new HashMap<>(commonAttributesMap);
            successMap.put(GENERIC_STATUS_KEY, "ok");
            this.sendAttributeCacheSuccess = new AttributeCache(PARTITION_ID_KEY,  successMap);

            Map<String, Object> failureMap = new HashMap<>(commonAttributesMap);
            failureMap.put(GENERIC_STATUS_KEY, "error");
            this.sendAttributeCacheFailure = new AttributeCache(PARTITION_ID_KEY,  failureMap);

            this.receiveAttributeCache = new AttributeCache(PARTITION_ID_KEY,  commonAttributesMap);
            this.sentEventsCounter = meter.createLongCounter("messaging.eventhubs.events.sent", "Number of sent events", "events");
            this.consumerLag = meter.createDoubleHistogram("messaging.eventhubs.consumer.lag", "Difference between local time when event was received and the local time it was enqueued on broker.", "sec");
        }
    }

    public void reportBatchSend(EventDataBatch batch, String partitionId, Throwable throwable, Context context) {
        if (isEnabled && sentEventsCounter.isEnabled()) {
            AttributeCache cache = throwable == null ? sendAttributeCacheSuccess : sendAttributeCacheFailure;
            sentEventsCounter.add(batch.getCount(), cache.getOrCreate(partitionId), context);
        }
    }

    public void reportReceive(PartitionEvent event) {
        if (isEnabled && consumerLag.isEnabled()) {
            Instant enqueuedTime = event.getData().getEnqueuedTime();
            double diff = 0d;
            if (enqueuedTime != null) {
                diff = Instant.now().toEpochMilli() - enqueuedTime.toEpochMilli();
                if (diff < 0) {
                    // time skew on machines
                    diff = 0;
                }
            }
            consumerLag.record(diff / 1000d,
                receiveAttributeCache.getOrCreate(event.getPartitionContext().getPartitionId()),
                Context.NONE);
        }
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
