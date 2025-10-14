// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.core.util.FluxUtil.monoError;

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
 *
 * <p><strong>Sample: Creating an {@link EventHubBufferedProducerAsyncClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the asynchronous client
 * {@link EventHubBufferedProducerAsyncClient}.  The {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host
 * name. It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal.
 * The producer is set to publish events every 60 seconds with a buffer size of 1500 events for each partition.  The
 * examples shown in this document use a credential object named DefaultAzureCredential for
 * authentication, which is appropriate for most scenarios, including local development and production
 * environments. Additionally, we recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation</a>.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubBufferedProducerAsyncClient client = new EventHubBufferedProducerClientBuilder&#40;&#41;
 *     .credential&#40;&quot;fully-qualified-namespace&quot;, &quot;event-hub-name&quot;, credential&#41;
 *     .onSendBatchSucceeded&#40;succeededContext -&gt; &#123;
 *         System.out.println&#40;&quot;Successfully published events to: &quot; + succeededContext.getPartitionId&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .onSendBatchFailed&#40;failedContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Failed to published events to %s. Error: %s%n&quot;,
 *             failedContext.getPartitionId&#40;&#41;, failedContext.getThrowable&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .maxWaitTime&#40;Duration.ofSeconds&#40;60&#41;&#41;
 *     .maxEventBufferLengthPerPartition&#40;1500&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.construct -->
 *
 * <p><strong>Sample: Enqueuing and publishing events</strong></p>
 *
 * <p>The following code sample demonstrates enqueuing a set of events in the buffered producer. The producer stores
 * these events in an internal queue and publishes them when
 * {@link EventHubBufferedProducerClientBuilder#maxWaitTime(Duration)} has elapsed, the buffer is full, or no more
 * events can fit into a batch.</p>
 *
 * <p>NOTE that {@code Mono<Integer>} returned must be subscribed to, or eventually subscribed to if chained to
 * reactive operators in order to start the operation.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.enqueueEvents-iterable -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubBufferedProducerAsyncClient client = new EventHubBufferedProducerClientBuilder&#40;&#41;
 *     .credential&#40;&quot;fully-qualified-namespace&quot;, &quot;event-hub-name&quot;, credential&#41;
 *     .onSendBatchSucceeded&#40;succeededContext -&gt; &#123;
 *         System.out.println&#40;&quot;Successfully published events to: &quot; + succeededContext.getPartitionId&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .onSendBatchFailed&#40;failedContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Failed to published events to %s. Error: %s%n&quot;,
 *             failedContext.getPartitionId&#40;&#41;, failedContext.getThrowable&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .buildAsyncClient&#40;&#41;;
 *
 * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;maple&quot;&#41;, new EventData&#40;&quot;aspen&quot;&#41;,
 *     new EventData&#40;&quot;oak&quot;&#41;&#41;;
 *
 * &#47;&#47; Enqueues the events to be published.
 * client.enqueueEvents&#40;events&#41;.subscribe&#40;numberOfEvents -&gt; &#123;
 *     System.out.printf&#40;&quot;There are currently: %d events in buffer.%n&quot;, numberOfEvents&#41;;
 * &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error occurred enqueueing events: &quot; + error&#41;;
 *     &#125;,
 *     &#40;&#41; -&gt; &#123;
 *         System.out.println&#40;&quot;Events successfully enqueued.&quot;&#41;;
 *     &#125;&#41;;
 *
 * &#47;&#47; Seconds later, enqueue another event.
 * client.enqueueEvent&#40;new EventData&#40;&quot;bonsai&quot;&#41;&#41;.subscribe&#40;numberOfEvents -&gt; &#123;
 *     System.out.printf&#40;&quot;There are %d events in the buffer.%n&quot;, numberOfEvents&#41;;
 * &#125;, error -&gt; &#123;
 *         System.err.println&#40;&quot;Error occurred enqueueing events: &quot; + error&#41;;
 *     &#125;,
 *     &#40;&#41; -&gt; &#123;
 *         System.out.println&#40;&quot;Event successfully enqueued.&quot;&#41;;
 *     &#125;&#41;;
 *
 * &#47;&#47; Causes any buffered events to be flushed before closing underlying connection.
 * client.close&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubbufferedproducerclient.enqueueEvents-iterable -->
 *
 * @see com.azure.messaging.eventhubs
 * @see EventHubBufferedProducerClientBuilder
 */
@ServiceClient(builder = EventHubBufferedProducerClientBuilder.class, isAsync = true)
public final class EventHubBufferedProducerAsyncClient implements Closeable {
    private static final SendOptions ROUND_ROBIN_SEND_OPTIONS = new SendOptions();

    private final ClientLogger logger = new ClientLogger(EventHubBufferedProducerAsyncClient.class);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final EventHubProducerAsyncClient client;
    private final BufferedProducerClientOptions clientOptions;
    private final PartitionResolver partitionResolver;
    private final Mono<Void> initialisationMono;
    private final Mono<String[]> partitionIdsMono;

    //  Key: partitionId.
    private final ConcurrentHashMap<String, EventHubBufferedPartitionProducer> partitionProducers
        = new ConcurrentHashMap<>();
    private final AmqpRetryOptions retryOptions;

    private final Tracer tracer;

    EventHubBufferedProducerAsyncClient(EventHubClientBuilder builder, BufferedProducerClientOptions clientOptions,
        PartitionResolver partitionResolver, AmqpRetryOptions retryOptions, Tracer tracer) {
        this.client = builder.buildAsyncProducerClient();
        this.clientOptions = clientOptions;
        this.partitionResolver = partitionResolver;
        this.retryOptions = retryOptions;

        final Mono<Void> partitionProducerFluxes = this.client.getEventHubProperties().flatMapMany(property -> {
            final String[] as = property.getPartitionIds().stream().toArray(String[]::new);
            return Flux.fromArray(as);
        }).map(partitionId -> {
            return partitionProducers.computeIfAbsent(partitionId, key -> createPartitionProducer(key));
        }).then();

        this.initialisationMono = partitionProducerFluxes.cache();

        this.partitionIdsMono = initialisationMono.then(Mono.fromCallable(() -> {
            return new ArrayList<>(partitionProducers.keySet()).toArray(new String[0]);
        })).cache();

        this.tracer = tracer;
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
        return initialisationMono.then(Mono.defer(() -> client.getEventHubProperties()));
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<String> getPartitionIds() {
        return partitionIdsMono.flatMapMany(ids -> Flux.fromArray(ids));
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
     * @throws IllegalArgumentException if {@code partitionId} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        if (Objects.isNull(partitionId)) {
            return monoError(logger, new NullPointerException("'partitionId' cannot be null."));
        } else if (CoreUtils.isNullOrEmpty(partitionId)) {
            return monoError(logger, new IllegalArgumentException("'partitionId' cannot be empty."));
        }

        return client.getPartitionProperties(partitionId);
    }

    /**
     * Gets the total number of events that are currently buffered and waiting to be published, across all partitions.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     */
    public int getBufferedEventCount() {
        return partitionProducers.values()
            .parallelStream()
            .mapToInt(producer -> producer.getBufferedEventCount())
            .sum();
    }

    /**
     * Gets the number of events that are buffered and waiting to be published for a given partition.
     *
     * @param partitionId The partition identifier.
     *
     * @return The number of events that are buffered and waiting to be published for a given partition.
     *
     * @throws NullPointerException if {@code partitionId} is null.
     * @throws IllegalArgumentException if {@code partitionId} is empty.
     */
    public int getBufferedEventCount(String partitionId) {
        final EventHubBufferedPartitionProducer producer = partitionProducers.get(partitionId);

        return producer != null ? producer.getBufferedEventCount() : 0;
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
     *
     * @throws NullPointerException if {@code eventData} is null.
     * @throws IllegalStateException if the producer was closed while queueing an event.
     */
    public Mono<Integer> enqueueEvent(EventData eventData) {
        return enqueueEvent(eventData, ROUND_ROBIN_SEND_OPTIONS);
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
     * @param options The set of options to apply when publishing this event.  If partitionKey and partitionId are
     *     not set, then the event is distributed round-robin amongst all the partitions.
     *
     * @return The total number of events that are currently buffered and waiting to be published, across all
     *     partitions.
     *
     * @throws NullPointerException if {@code eventData} or {@code options} is null.
     * @throws IllegalArgumentException if {@link SendOptions#getPartitionId() getPartitionId} is set and is not
     *     valid.
     * @throws IllegalStateException if the producer was closed while queueing an event.
     */
    public Mono<Integer> enqueueEvent(EventData eventData, SendOptions options) {
        if (eventData == null) {
            return monoError(logger, new NullPointerException("'eventData' cannot be null."));
        } else if (options == null) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        }

        if (!CoreUtils.isNullOrEmpty(options.getPartitionId())) {
            return partitionIdsMono.flatMap(ids -> {
                if (!partitionProducers.containsKey(options.getPartitionId())) {
                    return monoError(logger, new IllegalArgumentException(
                        "partitionId is not valid. Available ones: " + String.join(",", partitionProducers.keySet())));
                }

                final EventHubBufferedPartitionProducer producer
                    = partitionProducers.computeIfAbsent(options.getPartitionId(), key -> createPartitionProducer(key));

                return producer.enqueueEvent(eventData).then(Mono.fromCallable(() -> getBufferedEventCount()));
            });
        }

        if (options.getPartitionKey() != null) {
            return partitionIdsMono.flatMap(ids -> {
                final String partitionId = partitionResolver.assignForPartitionKey(options.getPartitionKey(), ids);

                final EventHubBufferedPartitionProducer producer = partitionProducers.get(partitionId);
                if (producer == null) {
                    return monoError(logger,
                        new IllegalArgumentException(String.format(
                            "Unable to find EventHubBufferedPartitionProducer for partitionId: %s when "
                                + "mapping partitionKey: %s to available partitions.",
                            partitionId, options.getPartitionKey())));
                }

                eventData.setPartitionKeyAnnotation(options.getPartitionKey());
                return producer.enqueueEvent(eventData).then(Mono.fromCallable(() -> getBufferedEventCount()));
            });
        } else {
            return partitionIdsMono.flatMap(ids -> {
                final String partitionId = partitionResolver.assignRoundRobin(ids);
                final EventHubBufferedPartitionProducer producer
                    = partitionProducers.computeIfAbsent(partitionId, key -> createPartitionProducer(key));

                eventData.setPartitionKeyAnnotation(options.getPartitionKey());
                return producer.enqueueEvent(eventData).then(Mono.fromCallable(() -> getBufferedEventCount()));
            });
        }
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
     *
     * @throws NullPointerException if {@code events} is null.
     * @throws IllegalStateException if the producer was closed while queueing an event.
     */
    public Mono<Integer> enqueueEvents(Iterable<EventData> events) {
        return enqueueEvents(events, ROUND_ROBIN_SEND_OPTIONS);
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
     *
     * @throws NullPointerException if {@code eventData} or {@code options} is null.
     * @throws IllegalArgumentException if {@link SendOptions#getPartitionId() getPartitionId} is set and is not
     *     valid.
     * @throws IllegalStateException if the producer was closed while queueing an event.
     */
    public Mono<Integer> enqueueEvents(Iterable<EventData> events, SendOptions options) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'eventData' cannot be null."));
        } else if (options == null) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        }

        final List<Mono<Integer>> enqueued = StreamSupport.stream(events.spliterator(), false)
            .map(event -> enqueueEvent(event, options))
            .collect(Collectors.toList());

        // concat subscribes to each publisher in sequence, so the last value will be the latest.
        return Flux.concat(enqueued).last();
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
        final List<Mono<Void>> flushOperations
            = partitionProducers.values().stream().map(value -> value.flush()).collect(Collectors.toList());

        return Flux.merge(flushOperations).then();
    }

    /**
     * Disposes of the producer and all its resources.
     */
    @Override
    public void close() {
        if (isClosed.getAndSet(true)) {
            return;
        }

        partitionProducers.values().forEach(partitionProducer -> partitionProducer.close());
        client.close();
    }

    private EventHubBufferedPartitionProducer createPartitionProducer(String partitionId) {
        final Supplier<Queue<EventData>> queueSupplier
            = Queues.get(clientOptions.getMaxEventBufferLengthPerPartition());
        final Queue<EventData> eventQueue = queueSupplier.get();
        final Sinks.Many<EventData> eventSink = Sinks.many().unicast().onBackpressureBuffer(eventQueue);

        return new EventHubBufferedPartitionProducer(client, partitionId, clientOptions, retryOptions, eventSink,
            tracer);
    }

    /**
     * A set of options to pass when creating the {@link EventHubBufferedProducerClient} or {@link
     * EventHubBufferedProducerAsyncClient}.
     */
    static class BufferedProducerClientOptions {
        private boolean enableIdempotentRetries = false;
        private int maxConcurrentSendsPerPartition = 1;

        private int maxEventBufferLengthPerPartition = 1500;
        private Duration maxWaitTime = Duration.ofSeconds(30);
        private Consumer<SendBatchFailedContext> sendFailedContext;
        private Consumer<SendBatchSucceededContext> sendSucceededContext;
        private int maxConcurrentSends = 1;

        boolean enableIdempotentRetries() {
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

        int getMaxEventBufferLengthPerPartition() {
            return maxEventBufferLengthPerPartition;
        }

        void maxEventBufferLengthPerPartition(int maxPendingEventCount) {
            this.maxEventBufferLengthPerPartition = maxPendingEventCount;
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
