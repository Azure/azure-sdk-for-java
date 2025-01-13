// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.LoadBalancingStrategy;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.SendOptions;
import com.azure.messaging.eventhubs.stress.util.SimulatedFailure;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.messaging.eventhubs.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.createMessagePayload;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.getBuilder;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.getProcessorBuilder;

/**
 * Test for EventProcessorClient that forwards messages
 */
@Component("EventForwarder")
public class EventForwarder extends EventHubsScenario {
    private final AtomicBoolean stopping = new AtomicBoolean(false);

    @Value("${MAX_BATCH_SIZE:10}")
    private int maxBatchSize;

    @Value("${MAX_WAIT_TIME_IN_MS:0}")
    private int maxWaitTimeInMs;

    @Value("${PREFETCH_COUNT:0}")
    private int prefetchCount;

    @Value("${ENABLE_CHECKPOINT:true}")
    private boolean enableCheckpoint;

    @Value("${FORWARD_EVENTHUBS_CONNECTION_STRING:#{null}}")
    private String forwardConnectionString;

    @Value("${FORWARD_EVENT_HUB_NAME:#{null}}")
    private String forwardEventHubName;

    @Value("${FORWARD_PARTITIONS_COUNT:8}")
    private int forwardPartitionsCount;

    @Value("${PROCESSOR_INSTANCES_COUNT:1}")
    private int processorInstancesCount;

    @Value("${PROCESSOR_FAILURE_RATIO:0}")
    private double processorFailureRatio;

    private byte[] expectedPayload;
    private EventHubProducerAsyncClient producerClient;

    @Override
    public void run() {
        producerClient = getForwardProducer();
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

    private EventHubProducerAsyncClient getForwardProducer() {
        // Gets the builder then overwrites the previously set values with new forwarder ones.
        final EventHubClientBuilder builder = getBuilder(options).connectionString(forwardConnectionString)
            .eventHubName(forwardEventHubName)
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(10)));

        return toClose(builder.buildAsyncProducerClient());
    }

    @SuppressWarnings("try")
    private void processBatch(EventBatchContext batchContext) {
        if (stopping.get()) {
            return;
        }

        Map<String, List<EventData>> batches = regroupEvents(batchContext);
        Flux.fromStream(batches.entrySet().stream().filter(e -> e.getValue() != null && !e.getValue().isEmpty()))
            .flatMap(entry -> {
                if (ThreadLocalRandom.current().nextDouble() < processorFailureRatio) {
                    return Mono.error(new SimulatedFailure());
                }

                return producerClient.send(entry.getValue(), new SendOptions().setPartitionKey(entry.getKey()))
                    .doOnCancel(() -> telemetryHelper.recordError("cancelled", "sendBatch", entry.getKey()))
                    .doOnError(err -> telemetryHelper.recordError(err, "sendBatch", entry.getKey()));
            })
            .parallel(batches.entrySet().size(), 1)
            .runOn(Schedulers.boundedElastic())
            .then()
            .block();

        if (enableCheckpoint) {
            batchContext.updateCheckpoint();
        }
    }

    private Map<String, List<EventData>> regroupEvents(EventBatchContext batchContext) {
        Map<String, List<EventData>> batches = new HashMap<>();
        for (EventData eventData : batchContext.getEvents()) {
            checkEvent(eventData, batchContext.getPartitionContext().getPartitionId());

            int forwardPartitionId = ThreadLocalRandom.current().nextInt(forwardPartitionsCount);
            batches.computeIfAbsent(String.valueOf(forwardPartitionId), k -> new ArrayList<>()).add(eventData);
        }
        return batches;
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
        span.setAttribute(AttributeKey.stringKey("forwardEventHubName"), forwardEventHubName);
        span.setAttribute(AttributeKey.longKey("forwardPartitionsCount"), forwardPartitionsCount);
        span.setAttribute(AttributeKey.longKey("processorInstancesCount"), processorInstancesCount);
        span.setAttribute(AttributeKey.doubleKey("processorFailureRatio"), processorFailureRatio);
    }
}
