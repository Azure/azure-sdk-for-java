// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannelClosedException;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpReceiveLinkProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.Closeable;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.CONNECTION_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.SIGNAL_TYPE_KEY;
import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * An <b>asynchronous</b> consumer responsible for reading {@link EventData} from either a specific Event Hub partition
 * or all partitions in the context of a specific consumer group.
 *
 * <p><strong>Creating an {@link EventHubConsumerAsyncClient}</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation -->
 * <pre>
 * &#47;&#47; The required parameters are `consumerGroup` and a way to authenticate with Event Hubs using credentials.
 * EventHubConsumerAsyncClient consumer = new EventHubClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;&quot;
 *         + &quot;SharedAccessKey=&#123;key&#125;;EntityPath=&#123;eh-name&#125;&quot;&#41;
 *     .consumerGroup&#40;&quot;consumer-group-name&quot;&#41;
 *     .buildAsyncConsumerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation -->
 *
 * <p><strong>Consuming events a single partition from Event Hub</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition -->
 * <pre>
 * &#47;&#47; Obtain partitionId from EventHubConsumerAsyncClient.getPartitionIds&#40;&#41;
 * String partitionId = &quot;0&quot;;
 * EventPosition startingPosition = EventPosition.latest&#40;&#41;;
 *
 * &#47;&#47; Keep a reference to `subscription`. When the program is finished receiving events, call
 * &#47;&#47; subscription.dispose&#40;&#41;. This will stop fetching events from the Event Hub.
 * Disposable subscription = consumer.receiveFromPartition&#40;partitionId, startingPosition&#41;
 *     .subscribe&#40;partitionEvent -&gt; &#123;
 *         PartitionContext partitionContext = partitionEvent.getPartitionContext&#40;&#41;;
 *         EventData event = partitionEvent.getData&#40;&#41;;
 *
 *         System.out.printf&#40;&quot;Received event from partition '%s'%n&quot;, partitionContext.getPartitionId&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Contents of event as string: '%s'%n&quot;, event.getBodyAsString&#40;&#41;&#41;;
 *     &#125;, error -&gt; System.err.print&#40;error.toString&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition -->
 *
 * <p><strong>Viewing latest partition information</strong></p>
 * <p>Latest partition information as events are received can by setting
 * {@link ReceiveOptions#setTrackLastEnqueuedEventProperties(boolean) setTrackLastEnqueuedEventProperties} to
 * {@code true}. As events come in, explore the {@link PartitionEvent} object.
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receiveFromPartition#string-eventposition-receiveoptions -->
 * <pre>
 * &#47;&#47; Set `setTrackLastEnqueuedEventProperties` to true to get the last enqueued information from the partition for
 * &#47;&#47; each event that is received.
 * ReceiveOptions receiveOptions = new ReceiveOptions&#40;&#41;
 *     .setTrackLastEnqueuedEventProperties&#40;true&#41;;
 *
 * &#47;&#47; Receives events from partition &quot;0&quot; as they come in.
 * consumer.receiveFromPartition&#40;&quot;0&quot;, EventPosition.earliest&#40;&#41;, receiveOptions&#41;
 *     .subscribe&#40;partitionEvent -&gt; &#123;
 *         LastEnqueuedEventProperties properties = partitionEvent.getLastEnqueuedEventProperties&#40;&#41;;
 *         System.out.printf&#40;&quot;Information received at %s. Last enqueued sequence number: %s%n&quot;,
 *             properties.getRetrievalTime&#40;&#41;,
 *             properties.getSequenceNumber&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receiveFromPartition#string-eventposition-receiveoptions -->
 *
 * <p><strong>Rate limiting consumption of events from Event Hub</strong></p>
 * <p>For event consumers that need to limit the number of events they receive at a given time, they can use
 * {@link BaseSubscriber#request(long)}.</p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition-basesubscriber -->
 * <pre>
 * consumer.receiveFromPartition&#40;partitionId, EventPosition.latest&#40;&#41;&#41;.subscribe&#40;new BaseSubscriber&lt;PartitionEvent&gt;&#40;&#41; &#123;
 *     private static final int NUMBER_OF_EVENTS = 5;
 *     private final AtomicInteger currentNumberOfEvents = new AtomicInteger&#40;&#41;;
 *
 *     &#64;Override
 *     protected void hookOnSubscribe&#40;Subscription subscription&#41; &#123;
 *         &#47;&#47; Tell the Publisher we only want 5 events at a time.
 *         request&#40;NUMBER_OF_EVENTS&#41;;
 *     &#125;
 *
 *     &#64;Override
 *     protected void hookOnNext&#40;PartitionEvent value&#41; &#123;
 *         &#47;&#47; Process the EventData
 *
 *         &#47;&#47; If the number of events we have currently received is a multiple of 5, that means we have reached the
 *         &#47;&#47; last event the Publisher will provide to us. Invoking request&#40;long&#41; here, tells the Publisher that
 *         &#47;&#47; the subscriber is ready to get more events from upstream.
 *         if &#40;currentNumberOfEvents.incrementAndGet&#40;&#41; % 5 == 0&#41; &#123;
 *             request&#40;NUMBER_OF_EVENTS&#41;;
 *         &#125;
 *     &#125;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition-basesubscriber -->
 *
 * <p><strong>Receiving from all partitions</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean -->
 * <pre>
 * &#47;&#47; Receives events from all partitions from the beginning of each partition.
 * consumer.receive&#40;true&#41;.subscribe&#40;partitionEvent -&gt; &#123;
 *     PartitionContext context = partitionEvent.getPartitionContext&#40;&#41;;
 *     EventData event = partitionEvent.getData&#40;&#41;;
 *     System.out.printf&#40;&quot;Event %s is from partition %s%n.&quot;, event.getSequenceNumber&#40;&#41;, context.getPartitionId&#40;&#41;&#41;;
 * &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean -->
 */
@ServiceClient(builder = EventHubClientBuilder.class, isAsync = true)
public class EventHubConsumerAsyncClient implements Closeable {
    private static final String RECEIVER_ENTITY_PATH_FORMAT = "%s/ConsumerGroups/%s/Partitions/%s";
    private static final ClientLogger LOGGER = new ClientLogger(EventHubConsumerAsyncClient.class);

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ReceiveOptions defaultReceiveOptions = new ReceiveOptions();
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final EventHubConnectionProcessor connectionProcessor;
    private final MessageSerializer messageSerializer;
    private final String consumerGroup;
    private final int prefetchCount;
    private final boolean isSharedConnection;
    private final Runnable onClientClosed;
    /**
     * Keeps track of the open partition consumers keyed by linkName. The link name is generated as: {@code
     * "partitionId_GUID"}. For receiving from all partitions, links are prefixed with {@code "all-GUID-partitionId"}.
     */
    private final ConcurrentHashMap<String, EventHubPartitionAsyncConsumer> openPartitionConsumers =
        new ConcurrentHashMap<>();

    EventHubConsumerAsyncClient(String fullyQualifiedNamespace, String eventHubName,
        EventHubConnectionProcessor connectionProcessor, MessageSerializer messageSerializer, String consumerGroup,
        int prefetchCount, boolean isSharedConnection, Runnable onClientClosed) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.connectionProcessor = connectionProcessor;
        this.messageSerializer = messageSerializer;
        this.consumerGroup = consumerGroup;
        this.prefetchCount = prefetchCount;
        this.isSharedConnection = isSharedConnection;
        this.onClientClosed = onClientClosed;
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return eventHubName;
    }

    /**
     * Gets the consumer group this consumer is reading events as a part of.
     *
     * @return The consumer group this consumer is reading events as a part of.
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventHubProperties> getEventHubProperties() {
        return connectionProcessor.flatMap(connection -> connection.getManagementNode())
            .flatMap(EventHubManagementNode::getEventHubProperties);
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<String> getPartitionIds() {
        return getEventHubProperties().flatMapMany(properties -> Flux.fromIterable(properties.getPartitionIds()));
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
        if (Objects.isNull(partitionId)) {
            return monoError(LOGGER, new NullPointerException("'partitionId' cannot be null."));
        } else if (partitionId.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'partitionId' cannot be an empty string."));
        }

        return connectionProcessor.flatMap(connection -> connection.getManagementNode())
            .flatMap(node -> node.getPartitionProperties(partitionId));
    }

    /**
     * Consumes events from a single partition starting at {@code startingPosition}.
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     *
     * @return A stream of events for this partition starting from {@code startingPosition}.
     *
     * @throws NullPointerException if {@code partitionId}, or {@code startingPosition} is null.
     * @throws IllegalArgumentException if {@code partitionId} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<PartitionEvent> receiveFromPartition(String partitionId, EventPosition startingPosition) {
        return receiveFromPartition(partitionId, startingPosition, defaultReceiveOptions);
    }

    /**
     * Consumes events from a single partition starting at {@code startingPosition} with a set of {@link ReceiveOptions
     * receive options}.
     *
     * <ul>
     * <li>If receive is invoked where {@link ReceiveOptions#getOwnerLevel()} has a value, then Event Hubs service will
     * guarantee only one active consumer exists per partitionId and consumer group combination. This receive operation
     * is sometimes referred to as an "Epoch Consumer".</li>
     * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
     * {@link ReceiveOptions#getOwnerLevel()} when invoking receive operations. This non-exclusive consumer is sometimes
     * referred to as a "Non-Epoch Consumer."</li>
     * </ul>
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     * @param receiveOptions Options when receiving events from the partition.
     *
     * @return A stream of events for this partition. If a stream for the events was opened before, the same position
     *     within that partition is returned. Otherwise, events are read starting from {@code startingPosition}.
     *
     * @throws NullPointerException if {@code partitionId}, {@code startingPosition}, {@code receiveOptions} is
     *     null.
     * @throws IllegalArgumentException if {@code partitionId} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<PartitionEvent> receiveFromPartition(String partitionId, EventPosition startingPosition,
        ReceiveOptions receiveOptions) {
        if (Objects.isNull(partitionId)) {
            return fluxError(LOGGER, new NullPointerException("'partitionId' cannot be null."));
        } else if (partitionId.isEmpty()) {
            return fluxError(LOGGER, new IllegalArgumentException("'partitionId' cannot be an empty string."));
        }

        if (Objects.isNull(startingPosition)) {
            return fluxError(LOGGER, new NullPointerException("'startingPosition' cannot be null."));
        }

        final String linkName = StringUtil.getRandomString(partitionId);
        return createConsumer(linkName, partitionId, startingPosition, receiveOptions);
    }

    /**
     * Consumes events from all partitions starting from the beginning of each partition.
     *
     * <p>This method is <b>not</b> recommended for production use; the {@link EventProcessorClient} should be used for
     * reading events from all partitions in a production scenario, as it offers a much more robust experience with
     * higher throughput.
     *
     * It is important to note that this method does not guarantee fairness amongst the partitions. Depending on service
     * communication, there may be a clustering of events per partition and/or there may be a noticeable bias for a
     * given partition or subset of partitions.</p>
     *
     *
     * @return A stream of events for every partition in the Event Hub starting from the beginning of each partition.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<PartitionEvent> receive() {
        return receive(true, defaultReceiveOptions);
    }

    /**
     * Consumes events from all partitions.
     *
     * <p>This method is <b>not</b> recommended for production use; the {@link EventProcessorClient} should be used for
     * reading events from all partitions in a production scenario, as it offers a much more robust experience with
     * higher throughput.
     *
     * It is important to note that this method does not guarantee fairness amongst the partitions. Depending on service
     * communication, there may be a clustering of events per partition and/or there may be a noticeable bias for a
     * given partition or subset of partitions.</p>
     *
     * @param startReadingAtEarliestEvent {@code true} to begin reading at the first events available in each
     *     partition; otherwise, reading will begin at the end of each partition seeing only new events as they are
     *     published.
     *
     * @return A stream of events for every partition in the Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<PartitionEvent> receive(boolean startReadingAtEarliestEvent) {
        return receive(startReadingAtEarliestEvent, defaultReceiveOptions);
    }

    /**
     * Consumes events from all partitions configured with a set of {@code receiveOptions}.
     *
     * <p>This method is <b>not</b> recommended for production use; the {@link EventProcessorClient} should be used for
     * reading events from all partitions in a production scenario, as it offers a much more robust experience with
     * higher throughput.
     *
     * It is important to note that this method does not guarantee fairness amongst the partitions. Depending on service
     * communication, there may be a clustering of events per partition and/or there may be a noticeable bias for a
     * given partition or subset of partitions.</p>
     *
     * <ul>
     * <li>If receive is invoked where {@link ReceiveOptions#getOwnerLevel()} has a value, then Event Hubs service will
     * guarantee only one active consumer exists per partitionId and consumer group combination. This receive operation
     * is sometimes referred to as an "Epoch Consumer".</li>
     * <li>Multiple consumers per partitionId and consumer group combination can be created by not setting
     * {@link ReceiveOptions#getOwnerLevel()} when invoking receive operations. This non-exclusive consumer is sometimes
     * referred to as a "Non-Epoch Consumer."</li>
     * </ul>
     *
     * @param startReadingAtEarliestEvent {@code true} to begin reading at the first events available in each
     *     partition; otherwise, reading will begin at the end of each partition seeing only new events as they are
     *     published.
     * @param receiveOptions Options when receiving events from each Event Hub partition.
     *
     * @return A stream of events for every partition in the Event Hub.
     *
     * @throws NullPointerException if {@code receiveOptions} is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<PartitionEvent> receive(boolean startReadingAtEarliestEvent, ReceiveOptions receiveOptions) {
        if (Objects.isNull(receiveOptions)) {
            return fluxError(LOGGER, new NullPointerException("'receiveOptions' cannot be null."));
        }

        final EventPosition startingPosition = startReadingAtEarliestEvent
            ? EventPosition.earliest()
            : EventPosition.latest();
        final String prefix = StringUtil.getRandomString("all");
        final Flux<PartitionEvent> allPartitionEvents = getPartitionIds().flatMap(partitionId -> {
            final String linkName = prefix + "-" + partitionId;

            return createConsumer(linkName, partitionId, startingPosition, receiveOptions);
        });

        return Flux.merge(allPartitionEvents);
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }
        openPartitionConsumers.forEach((key, value) -> value.close());
        openPartitionConsumers.clear();

        if (isSharedConnection) {
            onClientClosed.run();
        } else {
            connectionProcessor.dispose();
        }
    }

    private Flux<PartitionEvent> createConsumer(String linkName, String partitionId, EventPosition startingPosition,
        ReceiveOptions receiveOptions) {
        return openPartitionConsumers
            .computeIfAbsent(linkName,
                name -> createPartitionConsumer(name, partitionId, startingPosition, receiveOptions))
            .receive()
            .doFinally(signal -> removeLink(linkName, partitionId, signal));
    }

    private void removeLink(String linkName, String partitionId, SignalType signalType) {
        LOGGER.atInfo()
            .addKeyValue(LINK_NAME_KEY, linkName)
            .addKeyValue(PARTITION_ID_KEY, partitionId)
            .addKeyValue(SIGNAL_TYPE_KEY, signalType)
            .log("Receiving completed.");

        final EventHubPartitionAsyncConsumer consumer = openPartitionConsumers.remove(linkName);

        if (consumer != null) {
            consumer.close();
        }
    }

    private EventHubPartitionAsyncConsumer createPartitionConsumer(String linkName, String partitionId,
        EventPosition startingPosition, ReceiveOptions receiveOptions) {
        final String entityPath = String.format(Locale.US, RECEIVER_ENTITY_PATH_FORMAT,
            getEventHubName(), consumerGroup, partitionId);

        final AtomicReference<Supplier<EventPosition>> initialPosition = new AtomicReference<>(() -> startingPosition);

        // The Mono, when subscribed, creates a AmqpReceiveLink in the AmqpConnection emitted by the connectionProcessor
        //
        final Mono<AmqpReceiveLink> receiveLinkMono = connectionProcessor
            .flatMap(connection -> {
                LOGGER.atInfo()
                    .addKeyValue(LINK_NAME_KEY, linkName)
                    .addKeyValue(PARTITION_ID_KEY, partitionId)
                    .addKeyValue(CONNECTION_ID_KEY, connection.getId())
                    .log("Creating receive consumer for partition.");
                return connection.createReceiveLink(linkName, entityPath, initialPosition.get().get(), receiveOptions);
            });

        // A Mono that resubscribes to 'receiveLinkMono' to retry the creation of AmqpReceiveLink.
        //
        // The scenarios where this retry helps are -
        // [1]. When we try to create a link on a session being disposed but connection is healthy, the retry can
        //      eventually create a new session then the link.
        // [2]. When we try to create a new session (to host the new link) but on a connection being disposed,
        //      the retry can eventually receive a new connection and then proceed with creating session and link.
        //
        final Mono<AmqpReceiveLink> retryableReceiveLinkMono = RetryUtil.withRetry(receiveLinkMono.onErrorMap(
                RequestResponseChannelClosedException.class,
                e -> {
                    // When the current connection is being disposed, the connectionProcessor can produce
                    // a new connection if downstream request.
                    // In this context, treat RequestResponseChannelClosedException from the RequestResponseChannel scoped
                    // to the current connection being disposed as retry-able so that retry can obtain new connection.
                    return new AmqpException(true, e.getMessage(), e, null);
                }),
            connectionProcessor.getRetryOptions(),
            "Failed to create receive link " + linkName,
            true);

        // A Flux that produces a new AmqpReceiveLink each time it receives a request from the below
        // 'AmqpReceiveLinkProcessor'. Obviously, the processor requests a link when there is a downstream subscriber.
        // It also requests a new link (i.e. retry) when the current link it holds gets terminated
        // (e.g., when the service decides to close that link).
        //
        final Flux<AmqpReceiveLink> receiveLinkFlux = retryableReceiveLinkMono.repeat();

        final AmqpReceiveLinkProcessor linkMessageProcessor = receiveLinkFlux.subscribeWith(
            new AmqpReceiveLinkProcessor(entityPath, prefetchCount, connectionProcessor));

        return new EventHubPartitionAsyncConsumer(linkMessageProcessor, messageSerializer, getFullyQualifiedNamespace(),
            getEventHubName(), consumerGroup, partitionId, initialPosition,
            receiveOptions.getTrackLastEnqueuedEventProperties());
    }

    boolean isConnectionClosed() {
        return this.connectionProcessor.isChannelClosed();
    }
}
