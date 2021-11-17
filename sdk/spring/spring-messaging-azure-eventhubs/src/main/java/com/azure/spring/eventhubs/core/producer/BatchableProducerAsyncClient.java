// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.producer;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProperties;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.messaging.PartitionSupplier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.management.RuntimeErrorException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Batchable producer async client for sending events in batches.
 */
public class BatchableProducerAsyncClient implements EventHubProducer {

    private final EventHubProducerAsyncClient client;
    private final Duration maxWaitTime;
    private final CreateBatchOptions batchOptions;
    private final AtomicReference<Long> lastSendTime = new AtomicReference<>(Long.MIN_VALUE);
    private final AtomicReference<Long> nextSendTime = new AtomicReference<>(Long.MIN_VALUE);
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>();
    private volatile ScheduledFuture<?> scheduledTask;

    public BatchableProducerAsyncClient(EventHubProducerAsyncClient client, int maxBatchInBytes, Duration maxWaitTime) {
        this.client = client;
        this.maxWaitTime = maxWaitTime;
        this.batchOptions = new CreateBatchOptions().setMaximumSizeInBytes(maxBatchInBytes);
        this.scheduler.initialize();
    }

    @Override
    public Mono<Void> send(Flux<EventData> events) {
        return send(events, null);
    }

    @Override
    public synchronized Mono<Void> send(Flux<EventData> events, PartitionSupplier partitionSupplier) {
        List<EventData> eventList = events.collectList().block();
        if (currentBatch.get() == null) {
            currentBatch.set(client.createBatch(batchOptions).block());
        }
        EventDataBatch batch = currentBatch.get();
        for (EventData event : eventList) {

            if (!batch.tryAdd(event)) {
                if (batch.getCount() == 0) {
                    throw new IllegalArgumentException("Event is larger than maximum batch allowed size.");
                }
                client.send(batch).block();
                lastSendTime.set(System.currentTimeMillis());
                scheduleNextSendTask();
                currentBatch.set(client.createBatch().block());
                batch = currentBatch.get();
                batch.tryAdd(event);
            }
        }
        if (System.currentTimeMillis() > nextSendTime.get()) {
            if (scheduledTask != null && !scheduledTask.isDone()) {
                try {
                    scheduledTask.get();
                } catch (InterruptedException | ExecutionException exception) {
                    throw new RuntimeErrorException(new Error(exception.getCause()),
                        "Error while trying to send a batch of events when maxWaitTime is met.");
                }
            }
            scheduleNextSendTask();
        }

        return Mono.empty();
    }

    public Mono<Void> send(EventDataBatch batch) {
        return this.client.send(batch);
    }

    @Override
    public void close() {
        this.client.close();
    }

    private void scheduleNextSendTask() {
        nextSendTime.set(System.currentTimeMillis() + maxWaitTime.toMillis());
        scheduledTask = this.scheduler.schedule(() -> sendEventBatch(), new Date(nextSendTime.get()));
    }

    private void sendEventBatch() {
        client.send(currentBatch.get()).block();
        lastSendTime.set(System.currentTimeMillis());
        currentBatch.set(client.createBatch(batchOptions).block());
    }

    public Mono<EventHubProperties> getEventHubProperties() {
        return this.client.getEventHubProperties();
    }
}
