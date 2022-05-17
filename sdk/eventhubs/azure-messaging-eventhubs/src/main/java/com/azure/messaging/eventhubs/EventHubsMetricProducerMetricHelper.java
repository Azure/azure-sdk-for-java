package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.util.Context;
import com.azure.core.util.metrics.ClientLongCounter;
import com.azure.core.util.metrics.ClientLongHistogram;
import com.azure.core.util.metrics.ClientMeter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class EventHubsMetricProducerMetricHelper {
    private final static int ERROR_DIMENSIONS_LENGTH = AmqpErrorCondition.values().length + 2;
    private final static String DURATION_METRIC_NAME = "az.messaging.producer.send.duration";
    private final static String DURATION_METRIC_DESCRIPTION = "Duration of producer send call";
    private final static String DURATION_METRIC_UNIT = "ms";

    private final static String COUNTER_METRIC_NAME = "az.messaging.producer.send.events";
    private final static String COUNTER_METRIC_DESCRIPTION = "Duration of producer send call";
    private final static String COUNTER_METRIC_UNIT = "todo";

    private final ClientMeter meter;
    private final String fullyQualifiedNamespace;
    private final String eventHubName;

    // do we know all partitions ahead of time?
    private final ConcurrentMap<String, SendBatchMetrics[]> allMetrics = new ConcurrentHashMap<>();
    private final SendBatchMetrics[] nullPartition;

    public EventHubsMetricProducerMetricHelper(ClientMeter meter, String fullyQualifiedNamespace, String eventHubName) {
        this.meter = meter;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.nullPartition = createMetrics(null);
    }

    void recordSendBatch(long durationMs, long batchSize, Context context, String partitionId, boolean error, AmqpErrorCondition errorCode) {
        if (!meter.isEnabled()) {
            return;
        }

        SendBatchMetrics[] metrics = partitionId == null ? nullPartition : allMetrics.computeIfAbsent(partitionId, this::createMetrics);

        int index = ERROR_DIMENSIONS_LENGTH - 1; // ok
        if (error) {
            index = errorCode != null ? errorCode.ordinal() : ERROR_DIMENSIONS_LENGTH - 2;
        }

        metrics[index].record(duration., batchSize, context);
    }

    private SendBatchMetrics[] createMetrics(String partitionId) {
        SendBatchMetrics[] metrics = new SendBatchMetrics[ERROR_DIMENSIONS_LENGTH];
        for (int i = 0; i < ERROR_DIMENSIONS_LENGTH - 2; i ++) {
            metrics[i] =  new SendBatchMetrics(meter,
                getAttributes(partitionId, AmqpErrorCondition.values()[i].getErrorCondition()));
        }

        metrics[ERROR_DIMENSIONS_LENGTH - 2] = new SendBatchMetrics(meter, getAttributes(partitionId, "unknown"));
        metrics[ERROR_DIMENSIONS_LENGTH - 1] = new SendBatchMetrics(meter, getAttributes(partitionId, "ok"));
        return metrics;
    }

    private Map<String, Object> getAttributes(String partitionId, String errorCode) {
        Map<String, Object> attributes = new HashMap<>(4);
        attributes.put("az.messaging.destination", fullyQualifiedNamespace);
        attributes.put("az.messaging.entity", eventHubName);
        attributes.put("az.messaging.partition_id", partitionId == null ? "N/A" : partitionId);
        attributes.put("az.messaging.status_code", errorCode);

        return attributes;
    }

    private static class SendBatchMetrics {
        public final ClientLongHistogram sendDuration;
        public final ClientLongCounter eventCounter;

        public SendBatchMetrics(ClientMeter meter, Map<String, Object> attributes) {
            this.sendDuration  = meter.getLongHistogram(DURATION_METRIC_NAME, DURATION_METRIC_DESCRIPTION, DURATION_METRIC_UNIT, attributes);
            this.eventCounter = meter.getLongCounter(COUNTER_METRIC_NAME, COUNTER_METRIC_DESCRIPTION, COUNTER_METRIC_UNIT, attributes);
        }

        public void record(long duration, long eventCount, Context context) {
            sendDuration.record(duration, context);
            eventCounter.add(eventCount, context);
        }
    }
}
