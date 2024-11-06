// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.stress.util.SimulatedFailure;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.messaging.eventhubs.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.createMessagePayload;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.getProcessorBuilder;

/**
 * Test for EventProcessorClient
 */
@Component("EventProcessor")
public class EventProcessor extends EventHubsScenario {
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    @Value("${PROCESS_CALLBACK_DURATION_MAX_IN_MS:50}")
    private int processMessageDurationMaxInMs;

    @Value("${MAX_BATCH_SIZE:10}")
    private int maxBatchSize;

    @Value("${MAX_WAIT_TIME_IN_MS:0}")
    private int maxWaitTimeInMs;

    @Value("${PREFETCH_COUNT:0}")
    private int prefetchCount;

    @Value("${ENABLE_CHECKPOINT:true}")
    private boolean enableCheckpoint;

    @Value("${PROCESSOR_INSTANCES_COUNT:2}")
    private int processorInstancesCount;

    @Value("${PROCESSOR_FAILURE_RATIO:0}")
    private double processorFailureRatio;

    private byte[] expectedPayload;

    @Override
    public void run() {
        expectedPayload = createMessagePayload(options.getMessageSize()).toBytes();
        Duration maxWaitTime = maxWaitTimeInMs > 0 ? Duration.ofMillis(maxWaitTimeInMs) : null;

        EventProcessorClient[] processors = new EventProcessorClient[processorInstancesCount];
        for (int i = 0; i < processorInstancesCount; i++) {
            String processorId = String.valueOf(i);
            processors[i]
                = getProcessorBuilder(options, prefetchCount).loadBalancingStrategy(LoadBalancingStrategy.GREEDY)
                    .processEventBatch(batch -> telemetryHelper.instrumentProcess(() -> processBatch(batch),
                        "processBatch", batch.getPartitionContext().getPartitionId()), maxBatchSize, maxWaitTime)
                    .initialPartitionEventPosition(p -> EventPosition.earliest())
                    .processError(err -> telemetryHelper.recordError(err.getThrowable(),
                        String.format("processError[%s]", processorId), err.getPartitionContext().getPartitionId()))
                    .processPartitionClose(
                        closeContext -> telemetryHelper.recordPartitionClosedEvent(closeContext, processorId))
                    .processPartitionInitialization(initializationContext -> telemetryHelper
                        .recordPartitionInitializedEvent(initializationContext, processorId))
                    .buildEventProcessorClient();
            processors[i].start();
        }

        blockingWait(options.getTestDuration());

        stopping.set(true);
        blockingWait(Duration.ofSeconds(5));
        for (int i = 0; i < processorInstancesCount; i++) {
            processors[i].stop();
        }
    }

    @SuppressWarnings("try")
    private void processBatch(EventBatchContext batchContext) {
        if (stopping.get()) {
            return;
        }

        for (EventData eventData : batchContext.getEvents()) {
            checkEvent(eventData, batchContext.getPartitionContext().getPartitionId());
        }

        blockingWait(getWaitTime());
        if (processorFailureRatio > 0) {
            double failChance = ThreadLocalRandom.current().nextDouble();
            if (failChance < processorFailureRatio) {
                throw new SimulatedFailure();
            }
        }

        if (enableCheckpoint) {
            batchContext.updateCheckpoint();
        }
    }

    private Duration getWaitTime() {
        if (processMessageDurationMaxInMs != 0) {
            return Duration.ofMillis(ThreadLocalRandom.current().nextInt(processMessageDurationMaxInMs));
        }

        return Duration.ZERO;
    }

    private void checkEvent(EventData message, String partitionId) {
        byte[] payload = message.getBody();
        for (int i = 0; i < payload.length; i++) {
            if (payload[i] != expectedPayload[i]) {
                telemetryHelper.recordError("message corrupted", "checkMessage", partitionId);
                return;
            }
        }
    }

    @Override
    public void recordRunOptions(Span span) {
        span.setAttribute(AttributeKey.longKey("maxBatchSize"), maxBatchSize);
        span.setAttribute(AttributeKey.longKey("maxWaitTimeInMs"), maxWaitTimeInMs);
        span.setAttribute(AttributeKey.booleanKey("enableCheckpoint"), enableCheckpoint);
        span.setAttribute(AttributeKey.longKey("prefetchCount"), prefetchCount);
        span.setAttribute(AttributeKey.longKey("processMessageDurationMaxInMs"), processMessageDurationMaxInMs);
        span.setAttribute(AttributeKey.longKey("processorInstancesCount"), processorInstancesCount);
        span.setAttribute(AttributeKey.doubleKey("processorFailureRatio"), processorFailureRatio);
    }
}
