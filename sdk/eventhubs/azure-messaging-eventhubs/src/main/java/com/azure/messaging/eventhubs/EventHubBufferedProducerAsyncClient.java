// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

/**
 * A client responsible for publishing instances of {@link EventData} to a specific Event Hub.  Depending on the options
 * specified when events are enqueued, they may be automatically assigned to a partition, grouped according to the
 * specified partition key, or assigned a specifically requested partition.
 *
 * <p>
 * The {@link EventHubBufferedProducerAsyncClient} does not publish immediately, instead using a deferred model where
 * events are collected into a buffer so that they may be efficiently batched and published when the batch is full or
 * the {@link EventHubBufferedProducerClientBuilder#maxWaitTime(Duration) maxWaitTime} has elapsed with no new events
 * enqueued.
 * </p>
 * <p>
 * This model is intended to shift the burden of batch management from callers, at the cost of non-deterministic timing,
 * for when events will be published. There are additional trade-offs to consider, as well:
 * </p>
 * <ul>
 * <li>If the application crashes, events in the buffer will not have been published.  To
 * prevent data loss, callers are encouraged to track publishing progress using
 * {@link EventHubBufferedProducerClientBuilder#onSendBatchFailed(Consumer) onSendBatchFailed} and
 * {@link EventHubBufferedProducerClientBuilder#onSendBatchSucceeded(Consumer) onSendBatchSucceeded}.</li>
 * <li>Events specifying a partition key may be assigned a different partition than those
 * using the same key with other producers.</li>
 * <li>In the unlikely event that a partition becomes temporarily unavailable,
 * the {@link EventHubBufferedProducerAsyncClient} may take longer to recover than other producers.</li>
 * </ul>
 * <p>
 * In scenarios where it is important to have events published immediately with a deterministic outcome, ensure that
 * partition keys are assigned to a partition consistent with other publishers, or where maximizing availability is a
 * requirement, using {@link EventHubProducerAsyncClient} or {@link EventHubProducerClient} is recommended.
 * </p>
 */
@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class, isAsync = true)
public final class EventHubBufferedProducerAsyncClient implements Closeable {
    private final ClientLogger logger = new ClientLogger(EventHubBufferedProducerAsyncClient.class);
    private final EventHubAsyncClient client;
    private final EventHubClientBuilder builder;
    private final BufferedProducerClientOptions clientOptions;

    //  Key: partitionId.
    private final HashMap<String, ConcurrentLinkedDeque<EventDataBatch>> partitionBatchMap = new HashMap<>();
    private final Mono<Void> initialisationMono;

    EventHubBufferedProducerAsyncClient(EventHubClientBuilder builder, BufferedProducerClientOptions clientOptions) {
        this.builder = builder;
        this.client = builder.buildAsyncClient();
        this.clientOptions = clientOptions;

        initialisationMono = Mono.using(
            () -> builder.buildAsyncClient(),
            eventHubClient -> eventHubClient.getPartitionIds()
                .handle((partitionId, sink) -> {
                    try {
                        partitionBatchMap.put(partitionId, new ConcurrentLinkedDeque<>());
                        sink.complete();
                    } catch (Exception e) {
                        sink.error(e);
                    }
                }).then(),
            eventHubClient -> eventHubClient.close());
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

    /**
     * Gets the total number of events that are currently buffered and waiting to be published, across all partitions.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     */
    public int getBufferedEventCount() {
        return 0;
    }

    /**
     * Gets the number of events that are buffered and waiting to be published for a given partition.
     *
     * @param partitionId The partition identifier.
     *
     * @return The number of events that are buffered and waiting to be published for a given partition.
     */
    public int getBufferedEventCount(String partitionId) {
        return 0;
    }

    /**
     * Enqueues an {@link EventData} into the buffer to be published to the Event Hub.  If there is no capacity in the
     * buffer when this method is invoked, it will wait for space to become available and ensure that the {@code
     * eventData} has been enqueued.
     *
     * When this call returns, the {@code eventData} has been accepted into the buffer, but it may not have been
     * published yet. Publishing will take place at a nondeterministic point in the future as the buffer is processed.
     *
     * @param eventData The event to be enqueued into the buffer and, later, published.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     */
    public Mono<Integer> enqueueEvent(EventData eventData) {
        return null;
    }

    /**
     * Enqueues an {@link EventData} into the buffer to be published to the Event Hub.  If there is no capacity in the
     * buffer when this method is invoked, it will wait for space to become available and ensure that the {@code
     * eventData} has been enqueued.
     *
     * When this call returns, the {@code eventData} has been accepted into the buffer, but it may not have been
     * published yet. Publishing will take place at a nondeterministic point in the future as the buffer is processed.
     *
     * @param eventData The event to be enqueued into the buffer and, later, published.
     * @param options The set of options to apply when publishing this event.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     */
    public Mono<Integer> enqueueEvent(EventData eventData, SendOptions options) {
        return null;
    }

    /**
     * Enqueues a set of {@link EventData} into the buffer to be published to the Event Hub.  If there is insufficient
     * capacity in the buffer when this method is invoked, it will wait for space to become available and ensure that
     * all EventData in the {@code events} set have been enqueued.
     *
     * When this call returns, the {@code events} have been accepted into the buffer, but it may not have been published
     * yet. Publishing will take place at a nondeterministic point in the future as the buffer is processed.
     *
     * @param events The set of events to be enqueued into the buffer and, later, published.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     */
    public Mono<Integer> enqueueEvents(Iterable<EventData> events) {
        return null;
    }

    /**
     * Enqueues a set of {@link EventData} into the buffer to be published to the Event Hub.  If there is insufficient
     * capacity in the buffer when this method is invoked, it will wait for space to become available and ensure that
     * all EventData in the {@code events} set have been enqueued.
     *
     * When this call returns, the {@code events} have been accepted into the buffer, but it may not have been published
     * yet. Publishing will take place at a nondeterministic point in the future as the buffer is processed.
     *
     * @param events The set of events to be enqueued into the buffer and, later, published.
     * @param options The set of options to apply when publishing this event.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     */
    public Mono<Integer> enqueueEvents(Iterable<EventData> events, SendOptions options) {
        return null;
    }

    /**
     * Attempts to publish all events in the buffer immediately.  This may result in multiple batches being published,
     * the outcome of each of which will be individually reported by the
     * {@link EventHubBufferedProducerClientBuilder#onSendBatchFailed(Consumer)}
     * and {@link EventHubBufferedProducerClientBuilder#onSendBatchSucceeded(Consumer)} handlers.
     *
     * Upon completion of this method, the buffer will be empty.
     *
     * @return A mono that completes when the buffers are empty.
     */
    public Mono<Void> flush() {
        return null;
    }

    /**
     * Disposes of the producer and all its resources.
     */
    @Override
    public void close() {
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
        private int maxConcurrentSends;

        boolean isEnableIdempotentRetries() {
            return enableIdempotentRetries;
        }

        void setEnableIdempotentRetries(boolean enableIdempotentRetries) {
            this.enableIdempotentRetries = enableIdempotentRetries;
        }

        int getMaxConcurrentSends() {
            return maxConcurrentSends;
        }

        void setMaxConcurrentSends(int maxConcurrentSends) {
            this.maxConcurrentSends = maxConcurrentSends;
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

        Duration getMaxWaitTime() {
            return this.maxWaitTime;
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
