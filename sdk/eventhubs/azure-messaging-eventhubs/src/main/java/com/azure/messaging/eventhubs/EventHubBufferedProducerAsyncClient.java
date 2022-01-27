// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * An <strong>asynchronous</strong> producer responsible for transmitting {@link EventData} to a specific Event Hub
 * without building and managing batches.
 */
@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class, isAsync = true)
public final class EventHubBufferedProducerAsyncClient implements Closeable {
    private final EventHubAsyncClient client;
    private final EventHubProducerAsyncClient producer;
    private final BufferedProducerClientOptions clientOptions;
    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();

    //  Key: partitionId.
    private final ConcurrentHashMap<String, LinkedBlockingDeque<EventData>> partitionBatchMap = new ConcurrentHashMap<>();

    EventHubBufferedProducerAsyncClient(EventHubClientBuilder builder, BufferedProducerClientOptions clientOptions) {
        this.client = builder.buildAsyncClient();
        this.clientOptions = clientOptions;
        this.producer = this.client.createProducer();
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
     */
    public String getFullyQualifiedNamespace() {
        return client.getFullyQualifiedNamespace();
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return client.getEventHubName();
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventHubProperties> getEventHubProperties() {
        return client.getProperties();
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    public Flux<String> getPartitionIds() {
        return client.getPartitionIds();
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     *
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     *
     * @throws NullPointerException if {@code partitionId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        return client.getPartitionProperties(partitionId);
    }

    /**
     * Retrieves the quantity of events in the buffered client that have not been sent.
     *
     * @return The quantity of events.
     */
    public int getBufferedEventCount() {
        AtomicInteger count = new AtomicInteger();
        partitionBatchMap.values().forEach(queue -> count.addAndGet(queue.size()));
        return count.get();
    }

    /**
     * Retrieves the quantity of events in the buffered client that will send to a specific partition.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     *
     * @return the quantity of events for the specific partition.
     */
    public int getBufferedEventCount(String partitionId) {
        return partitionBatchMap.get(partitionId).size();
    }

    /**
     * Enqueue a single event without send options, will be sent to event hub by default send options.
     *
     * @param eventData The {@link EventData} to store in client temporarily to send to event hub.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     * @return Gets a {@link Mono} that completes when eventData enqueued.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> enqueueEvent(EventData eventData) {
        return enqueueEvent(eventData, DEFAULT_SEND_OPTIONS);
    }

    /**
     * Enqueue a single event with send options.
     *
     * @param eventData The {@link EventData} will be stored in client temporarily and sent to event hub.
     * @param options A set of options used to configure the {@link EventDataBatch}.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     * @return Gets a {@link Mono} that completes when eventData enqueued.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> enqueueEvent(EventData eventData, SendOptions options) {
        List<EventData> list = new ArrayList<>();
        list.add(eventData);
        return enqueueEvents(list, options);
    }

    /**
     * Enqueue iterable events without send options, will be sent to event hub by default send options.
     *
     * @param events Events will be stored in client temporarily and sent to event hub.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     * @return Gets a {@link Mono} that completes when eventData enqueued.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> enqueueEvents(Iterable<EventData> events) {
        return enqueueEvents(events, DEFAULT_SEND_OPTIONS);
    }

    /**
     * Enqueue iterable events with send options.
     *
     * @param events Events will be stored in client temporarily and sent to event hub.
     * @param options A set of options used to configure the {@link EventDataBatch}.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     * @return Gets a {@link Mono} that completes when eventData enqueued.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> enqueueEvents(Iterable<EventData> events, SendOptions options) {
        Objects.requireNonNull(events, "'eventData' cannot be null.");
        ArrayList<EventData> list = new ArrayList<>();
        events.forEach(list::add);
        addBatchToPartitionMap(options.getPartitionId(), list);
        return Mono.empty();
    }

    private void addBatchToPartitionMap(String partitionId, List<EventData> list) {
        partitionBatchMap.compute(partitionId, (k, v) -> {
            if (v != null) {
                v.addAll(list);
            } else {
                LinkedBlockingDeque<EventData> queue = new LinkedBlockingDeque<>(list);
                v = queue;
                Flux.generate((sink) -> {
                    try {
                        EventData eventData = queue.take();
                        sink.next(eventData);
                    } catch (InterruptedException e) {
                        sink.error(e);
                    }
                }).publishOn(Schedulers.newSingle(partitionId))
                    .map(event -> (EventData) event)
                    .windowTimeout(clientOptions.maxPendingEventCount, clientOptions.maxWaitTime)
                    .flatMap(Flux::collectList)
                    .flatMap(eventList -> {
                        if (eventList.size() == 0) {
                            return Mono.empty();
                        }
                        CreateBatchOptions options = new CreateBatchOptions();
                        options.setPartitionId(partitionId);
                        AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>();
                        return producer.createBatch(options).map(eventDataBatch -> {
                            currentBatch.set(eventDataBatch);
                            return Flux.fromIterable(eventList).flatMap(event -> {
                                EventDataBatch batch = currentBatch.get();
                                if (batch.tryAdd(event)) {
                                    return Mono.empty();
                                }
                                return Mono.when(
                                    producer.send(batch).doOnEach(signal -> {
                                        if (signal.hasError()) {
                                            this.clientOptions.getSendFailedContext()
                                                .accept(new SendBatchFailedContext(batch.getEvents(), batch.getPartitionId(), signal.getThrowable()));
                                        } else {
                                            this.clientOptions.getSendSucceededContext()
                                                .accept(new SendBatchSucceededContext(batch.getEvents(), batch.getPartitionId()));
                                        }
                                    }),
                                    producer.createBatch(options).map(newBatch -> {
                                        currentBatch.set(newBatch);
                                        if (!newBatch.tryAdd(event)) {
                                            this.clientOptions.getSendFailedContext()
                                                .accept(
                                                    new SendBatchFailedContext(
                                                        eventDataBatch.getEvents(),
                                                        eventDataBatch.getPartitionId(),
                                                        new IllegalArgumentException(String.format(Locale.US, "Size of the payload exceeded maximum message size: %s kb", options.getMaximumSizeInBytes() / 1024)))
                                                );
                                        }
                                        return newBatch;
                                    })
                                );
                            });
                        }).then().doFinally(sig -> {
                            EventDataBatch batch = currentBatch.getAndSet(null);
                            producer.send(batch).doOnEach(signal -> {
                                if (signal.hasError()) {
                                    this.clientOptions.getSendFailedContext()
                                        .accept(new SendBatchFailedContext(batch.getEvents(), batch.getPartitionId(), signal.getThrowable()));
                                } else {
                                    this.clientOptions.getSendSucceededContext()
                                        .accept(new SendBatchSucceededContext(batch.getEvents(), batch.getPartitionId()));
                                }
                            });
                        });
                    })
                    .subscribeOn(Schedulers.newSingle(partitionId))
                    .subscribe();
            }
            return v;
        });
    }

    // TODO
    /**
     * Send all events stored in the client to event hub.
     *
     * @return Gets a {@link Mono} that completes when all events have been sent or reach max wait time.
     */
    public Mono<Void> flush() {
        return Mono.empty();
    }

    /**
     * Disposes of the {@link EventHubBufferedProducerAsyncClient}. This operation will trigger {@link EventHubBufferedProducerAsyncClient#flush()} operation.
     */
    @Override
    public void close() {
        flush().block();
        client.close();
    }

    /**
     * A set of options to pass when creating the {@link EventHubBufferedProducerClient} or {@link
     * EventHubBufferedProducerAsyncClient}.
     */
    static class BufferedProducerClientOptions {
        private boolean enableIdempotentRetries = false;
        private int maxConcurrentSendsPerPartition = 1;

        private int maxPendingEventCount = 1500;
        private Duration maxWaitTime;
        private Consumer<SendBatchFailedContext> sendFailedContext;
        private Consumer<SendBatchSucceededContext> sendSucceededContext;

        boolean isEnableIdempotentRetries() {
            return enableIdempotentRetries;
        }

        void setEnableIdempotentRetries(boolean enableIdempotentRetries) {
            this.enableIdempotentRetries = enableIdempotentRetries;
        }

        int getMaxConcurrentSendsPerPartition() {
            return maxConcurrentSendsPerPartition;
        }

        void setMaxConcurrentSendsPerPartition(int maxConcurrentSendsPerPartition) {
            this.maxConcurrentSendsPerPartition = maxConcurrentSendsPerPartition;
        }

        int getMaxPendingEventCount() {
            return maxPendingEventCount;
        }

        void setMaxPendingEventCount(int maxPendingEventCount) {
            this.maxPendingEventCount = maxPendingEventCount;
        }

        void setMaxWaitTime(Duration maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
        }

        Consumer<SendBatchFailedContext> getSendFailedContext() {
            return sendFailedContext;
        }

        void setSendFailedContext(Consumer<SendBatchFailedContext> sendFailedContext) {
            this.sendFailedContext = sendFailedContext;
        }

        Consumer<SendBatchSucceededContext> getSendSucceededContext() {
            return sendSucceededContext;
        }

        void setSendSucceededContext(Consumer<SendBatchSucceededContext> sendSucceededContext) {
            this.sendSucceededContext = sendSucceededContext;
        }
    }
}
