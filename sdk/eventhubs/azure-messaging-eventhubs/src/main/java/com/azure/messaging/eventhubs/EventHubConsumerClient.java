// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.SynchronousEventSubscriber;
import com.azure.messaging.eventhubs.implementation.SynchronousReceiveWork;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsConsumerInstrumentation;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.Closeable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;

/**
 * <p>A <b>synchronous</b> consumer responsible for reading {@link EventData} from an Event Hub partition in the context of
 * a specific consumer group.</p>
 *
 * <p>Most receive operations contain a parameter {@code maxWaitTime}.  The iterable is returned when either
 * {@code maxWaitTime} has elapsed or {@code numberOfEvents} have been received.  It is possible to have an empty
 * iterable if no events were received in that time frame.  {@link #receiveFromPartition(String, int, EventPosition)}
 * does not have a parameter for {@code maxWaitTime}, consequently, it can take a long time to return results if
 * {@code numberOfEvents} is too high and there is low traffic in that Event Hub.</p>
 *
 * <p>The examples shown in this document use a credential object named DefaultAzureCredential for authentication,
 * which is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Creating a synchronous consumer</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the synchronous client {@link EventHubConsumerClient}.
 * The {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host name. It is listed under the "Essentials"
 * panel after navigating to the Event Hubs Namespace via Azure Portal. The {@code consumerGroup} is found by
 * navigating to the Event Hub instance, and selecting "Consumer groups" under the "Entities" panel.  The
 * {@link EventHubClientBuilder#consumerGroup(String)} is required for creating consumer clients. </p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerclient.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubConsumerClient consumer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .consumerGroup&#40;EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME&#41;
 *     .buildConsumerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerclient.construct -->
 *
 * <p><strong>Sample: Consuming events from a single partition</strong></p>
 *
 * <p>Events from a single partition can be consumed using {@link #receiveFromPartition(String, int, EventPosition)} or
 * {@link #receiveFromPartition(String, int, EventPosition, Duration)}. The call to {@code receiveFromPartition}
 * completes and returns an {@link IterableStream} when either the maximum number of events is received, or the
 * timeout has elapsed.  It is possible to have an empty iterable returned if there were no events received in that
 * duration.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubConsumerClient consumer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .consumerGroup&#40;EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME&#41;
 *     .buildConsumerClient&#40;&#41;;
 *
 * Instant twelveHoursAgo = Instant.now&#40;&#41;.minus&#40;Duration.ofHours&#40;12&#41;&#41;;
 * EventPosition startingPosition = EventPosition.fromEnqueuedTime&#40;twelveHoursAgo&#41;;
 * String partitionId = &quot;0&quot;;
 *
 * &#47;&#47; Reads events from partition '0' and returns the first 100 received or until the 30 seconds has elapsed.
 * IterableStream&lt;PartitionEvent&gt; events = consumer.receiveFromPartition&#40;partitionId, 100,
 *     startingPosition, Duration.ofSeconds&#40;30&#41;&#41;;
 *
 * Long lastSequenceNumber = -1L;
 * for &#40;PartitionEvent partitionEvent : events&#41; &#123;
 *     &#47;&#47; For each event, perform some sort of processing.
 *     System.out.print&#40;&quot;Event received: &quot; + partitionEvent.getData&#40;&#41;.getSequenceNumber&#40;&#41;&#41;;
 *     lastSequenceNumber = partitionEvent.getData&#40;&#41;.getSequenceNumber&#40;&#41;;
 * &#125;
 *
 * &#47;&#47; Figure out what the next EventPosition to receive from is based on last event we processed in the stream.
 * &#47;&#47; If lastSequenceNumber is -1L, then we didn't see any events the first time we fetched events from the
 * &#47;&#47; partition.
 * if &#40;lastSequenceNumber != -1L&#41; &#123;
 *     EventPosition nextPosition = EventPosition.fromSequenceNumber&#40;lastSequenceNumber, false&#41;;
 *
 *     &#47;&#47; Gets the next set of events from partition '0' to consume and process.
 *     IterableStream&lt;PartitionEvent&gt; nextEvents = consumer.receiveFromPartition&#40;partitionId, 100,
 *         nextPosition, Duration.ofSeconds&#40;30&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration -->
 *
 * @see com.azure.messaging.eventhubs
 * @see EventHubClientBuilder
 */
@ServiceClient(builder = EventHubClientBuilder.class)
public class EventHubConsumerClient implements Closeable {
    private static final ClientLogger LOGGER = new ClientLogger(EventHubConsumerClient.class);

    private final EventHubConsumerAsyncClient consumer;
    private final ReceiveOptions defaultReceiveOptions = new ReceiveOptions();
    private final Duration timeout;
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final EventHubsConsumerInstrumentation instrumentation;
    private final SynchronousPartitionReceiver syncReceiver;

    EventHubConsumerClient(EventHubConsumerAsyncClient consumer, Duration tryTimeout) {
        Objects.requireNonNull(tryTimeout, "'tryTimeout' cannot be null.");

        this.consumer = Objects.requireNonNull(consumer, "'consumer' cannot be null.");
        this.timeout = tryTimeout;
        this.instrumentation = consumer.getInstrumentation();
        this.syncReceiver = new SynchronousPartitionReceiver(consumer); // used in V2 mode.
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return consumer.getFullyQualifiedNamespace();
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return consumer.getEventHubName();
    }

    /**
     * Gets the consumer group this consumer is reading events as a part of.
     *
     * @return The consumer group this consumer is reading events as a part of.
     */
    public String getConsumerGroup() {
        return consumer.getConsumerGroup();
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventHubProperties getEventHubProperties() {
        return consumer.getEventHubProperties().block();
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return The set of identifiers for the partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<String> getPartitionIds() {
        return new IterableStream<>(consumer.getPartitionIds());
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
    public PartitionProperties getPartitionProperties(String partitionId) {
        return consumer.getPartitionProperties(partitionId).block();
    }

    /**
     * Receives a batch of {@link PartitionEvent events} from the Event Hub partition.
     *
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param partitionId Identifier of the partition to read events from.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     *
     * @return A set of {@link PartitionEvent} that was received. The iterable contains up to
     *     {@code maximumMessageCount} events. If a stream for the events was opened before, the same position within
     *     that partition is returned. Otherwise, events are read starting from {@code startingPosition}.
     *
     * @throws NullPointerException if {@code partitionId}, or {@code startingPosition} is null.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1, or if {@code partitionId} is an
     *     empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<PartitionEvent> receiveFromPartition(String partitionId, int maximumMessageCount,
        EventPosition startingPosition) {
        return receiveFromPartition(partitionId, maximumMessageCount, startingPosition, timeout);
    }

    /**
     * Receives a batch of {@link PartitionEvent events} from the Event Hub partition.
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     * @param maximumWaitTime The maximum amount of time to wait to build up the requested message count for the
     *     batch; if not specified, the default wait time specified when the consumer was created will be used.
     *
     * @return A set of {@link PartitionEvent} that was received. The iterable contains up to
     *     {@code maximumMessageCount} events.
     *
     * @throws NullPointerException if {@code partitionId}, {@code maximumWaitTime}, or {@code startingPosition} is
     *     {@code null}.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1 or {@code maximumWaitTime} is
     *     zero or a negative duration.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<PartitionEvent> receiveFromPartition(String partitionId, int maximumMessageCount,
        EventPosition startingPosition, Duration maximumWaitTime) {
        if (Objects.isNull(maximumWaitTime)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'maximumWaitTime' cannot be null."));
        } else if (Objects.isNull(startingPosition)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'startingPosition' cannot be null."));
        } else if (Objects.isNull(partitionId)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'partitionId' cannot be null."));
        }

        if (partitionId.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'partitionId' cannot be empty."));
        }
        if (maximumMessageCount < 1) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'maximumMessageCount' cannot be less than 1."));
        } else if (maximumWaitTime.isNegative() || maximumWaitTime.isZero()) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'maximumWaitTime' cannot be zero or less."));
        }

        if (consumer.isV2()) {
            // Sync receiver instrumentation is implemented in the SynchronousPartitionReceiver class
            return syncReceiver.receive(partitionId, startingPosition, defaultReceiveOptions, maximumMessageCount,
                maximumWaitTime);
        }

        Flux<PartitionEvent> events =
            Flux.create(emitter -> queueWork(partitionId, maximumMessageCount, startingPosition, maximumWaitTime, defaultReceiveOptions,
                emitter));

        return new IterableStream<>(instrumentation.syncReceive(events, partitionId));
    }

    /**
     * Receives a batch of {@link PartitionEvent events} from the Event Hub partition.
     *
     * @param partitionId Identifier of the partition to read events from.
     * @param maximumMessageCount The maximum number of messages to receive in this batch.
     * @param startingPosition Position within the Event Hub partition to begin consuming events.
     * @param maximumWaitTime The maximum amount of time to wait to build up the requested message count for the
     *     batch; if not specified, the default wait time specified when the consumer was created will be used.
     * @param receiveOptions Options when receiving events from the partition.

     * @return A set of {@link PartitionEvent} that was received. The iterable contains up to
     *     {@code maximumMessageCount} events.
     *
     * @throws NullPointerException if {@code maximumWaitTime}, {@code startingPosition}, {@code partitionId}, or
     *     {@code receiveOptions} is {@code null}.
     * @throws IllegalArgumentException if {@code maximumMessageCount} is less than 1 or {@code maximumWaitTime} is
     *     zero or a negative duration.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public IterableStream<PartitionEvent> receiveFromPartition(String partitionId, int maximumMessageCount,
        EventPosition startingPosition, Duration maximumWaitTime, ReceiveOptions receiveOptions) {
        if (Objects.isNull(maximumWaitTime)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'maximumWaitTime' cannot be null."));
        } else if (Objects.isNull(startingPosition)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'startingPosition' cannot be null."));
        } else if (Objects.isNull(partitionId)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'partitionId' cannot be null."));
        } else if (Objects.isNull(receiveOptions)) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'receiveOptions' cannot be null."));
        }

        if (partitionId.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'partitionId' cannot be empty."));
        }
        if (maximumMessageCount < 1) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'maximumMessageCount' cannot be less than 1."));
        } else if (maximumWaitTime.isNegative() || maximumWaitTime.isZero()) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'maximumWaitTime' cannot be zero or less."));
        }

        if (consumer.isV2()) {
            // Sync receiver instrumentation is implemented in the SynchronousPartitionReceiver class
            return syncReceiver.receive(partitionId, startingPosition, receiveOptions, maximumMessageCount,
                maximumWaitTime);
        }

        Flux<PartitionEvent> events = Flux.create(emitter -> {
            queueWork(partitionId, maximumMessageCount, startingPosition, maximumWaitTime, receiveOptions, emitter);
        });

        return new IterableStream<>(instrumentation.syncReceive(events, partitionId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        syncReceiver.dispose();
        consumer.close();
    }

    /**
     * Given an {@code emitter}, queues that work in {@link SynchronousEventSubscriber}. If the synchronous job has not
     * been created, will initialise it.
     */
    private void queueWork(String partitionId, int maximumMessageCount, EventPosition startingPosition,
        Duration maximumWaitTime, ReceiveOptions receiveOptions, FluxSink<PartitionEvent> emitter) {
        final long id = idGenerator.getAndIncrement();
        final SynchronousReceiveWork work = new SynchronousReceiveWork(id, maximumMessageCount, maximumWaitTime,
            emitter);
        final SynchronousEventSubscriber syncSubscriber = new SynchronousEventSubscriber(work);
        LOGGER.atInfo()
            .addKeyValue(PARTITION_ID_KEY, partitionId)
            .log("Started synchronous event subscriber.");

        consumer.receiveFromPartition(partitionId, startingPosition, receiveOptions).subscribeWith(syncSubscriber);
    }

    /**
     * Gets the client identifier.
     *
     * @return The unique identifier string for current client.
     */
    public String getIdentifier() {
        return this.consumer.getIdentifier();
    }
}
