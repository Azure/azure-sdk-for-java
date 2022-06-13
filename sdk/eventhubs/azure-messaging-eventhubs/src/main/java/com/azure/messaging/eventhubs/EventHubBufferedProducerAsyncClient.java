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

@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class)
public final class EventHubBufferedProducerAsyncClient implements Closeable {
    private final ClientLogger logger  = new ClientLogger(EventHubBufferedProducerAsyncClient.class);
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

    public int getBufferedEventCount() {
        return 0;
    }

    public int getBufferedEventCount(String partitionId) {
        return 0;
    }

    public Mono<Void> enqueueEvent(EventData eventData) {
        return null;
    }

    public Mono<Void> enqueueEvent(EventData eventData, SendOptions options) {
        return null;
    }

    public Mono<Void> enqueueEvents(Iterable<EventData> events) {
        return null;
    }

    public Mono<Void> enqueueEvents(Iterable<EventData> events, SendOptions options) {
        return null;
    }

    public Mono<Void> flush() {
        return null;
    }

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

        //TODO (conniey): Figure out what the other defaults are.
        private int maxPendingEventCount = 10;
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
