// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class)
public final class EventHubBufferedProducerAsyncClient implements Closeable {
    private final ClientLogger logger  = new ClientLogger(EventHubBufferedProducerAsyncClient.class);
    private final EventHubAsyncClient client;
    private final EventHubClientBuilder builder;
    private final EventHubProducerAsyncClient producer;
    private final BufferedProducerClientOptions clientOptions;
    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();


    //  Key: partitionId.
    private final HashMap<String, ConcurrentLinkedDeque<EventDataBatch>> partitionBatchMap = new HashMap<>();

    EventHubBufferedProducerAsyncClient(EventHubClientBuilder builder, BufferedProducerClientOptions clientOptions) {
        this.builder = builder;
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
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

    public int getBufferedEventCount() {
        AtomicInteger total = new AtomicInteger();
        partitionBatchMap.forEach((String key, ConcurrentLinkedDeque<EventDataBatch> item) -> {
            total.addAndGet(item.size());
        });
        return total.get();
    }

    public int getBufferedEventCount(String partitionId) {
        return partitionBatchMap.get(partitionId).size();
    }

    public Mono<Void> enqueueEvent(EventData eventData) {
        return enqueueEvent(eventData, DEFAULT_SEND_OPTIONS);
    }

    public Mono<Void> enqueueEvent(EventData eventData, SendOptions options) {
        Objects.requireNonNull(eventData, "'eventData' cannot be null.");
        CreateBatchOptions createBatchOptions = new CreateBatchOptions().setPartitionId(options.getPartitionId()).setPartitionKey(options.getPartitionKey());
        EventDataBatch eventDataBatch = this.producer.createBatch(createBatchOptions).block();
        if (eventDataBatch.tryAdd(eventData)) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "Size of the payload exceeded maximum message size: %s kb",
                createBatchOptions.getMaximumSizeInBytes()/1024));
        }
        addBatchToPartitionMap(options.getPartitionId(), eventDataBatch);
        return publish(options.getPartitionId());
    }

    public Mono<Void> enqueueEvents(Iterable<EventData> events) {
        return enqueueEvents(events, DEFAULT_SEND_OPTIONS);
    }

    public Mono<Void> enqueueEvents(Iterable<EventData> events, SendOptions options) {
        Objects.requireNonNull(events, "'eventData' cannot be null.");
        CreateBatchOptions createBatchOptions = new CreateBatchOptions().setPartitionId(options.getPartitionId()).setPartitionKey(options.getPartitionKey());
        AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch(createBatchOptions).block());

        events.forEach(item -> {
            Objects.requireNonNull(item, "'eventData' cannot be null.");
            if (!currentBatch.get().tryAdd(item)) {
                addBatchToPartitionMap(options.getPartitionId(), currentBatch.get());
                currentBatch.set(producer.createBatch(createBatchOptions).block());
                if (!currentBatch.get().tryAdd(item)) {
                    throw new IllegalArgumentException(String.format(Locale.US,
                        "Size of the payload exceeded maximum message size: %s kb",
                        createBatchOptions.getMaximumSizeInBytes()/1024));
                }
            }
        });
        EventDataBatch batch = currentBatch.getAndSet(null);
        addBatchToPartitionMap(options.getPartitionId(), batch);
        return publish(options.getPartitionId());
    }

    private void addBatchToPartitionMap(String partitionId, EventDataBatch eventDataBatch) {
        partitionBatchMap.compute(partitionId, (k, v) -> {
            if (v == null) {
                v =  new ConcurrentLinkedDeque<>();
            }
            v.add(eventDataBatch);
            return v;
        });
    }

    public Mono<Void> flush() {
        return Mono.when(Flux.fromIterable(partitionBatchMap.values()).flatMap((cdq) -> Flux.fromIterable(cdq).flatMap(this.producer::send)));

    }

    private Mono<Void> publish(String partitionId) {
        EventDataBatch dataBatch = partitionBatchMap.get(partitionId).poll();
        List<EventDataBatch> list = new ArrayList<>();
        while (dataBatch != null) {
            list.add(dataBatch);
            dataBatch = partitionBatchMap.get(partitionId).poll();
        }
        return Mono.when(Flux.fromIterable(list).flatMap(this.producer::send));
    }

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
