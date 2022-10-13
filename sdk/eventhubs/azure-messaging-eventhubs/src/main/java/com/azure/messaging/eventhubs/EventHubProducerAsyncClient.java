// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubsMetricsProvider;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.azure.core.amqp.implementation.RetryUtil.getRetryPolicy;
import static com.azure.core.amqp.implementation.RetryUtil.withRetry;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.AZ_NAMESPACE_VALUE;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.AZ_TRACING_SERVICE_NAME;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.MAX_MESSAGE_LENGTH_BYTES;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.PARTITION_KEY_KEY;

/**
 * An <b>asynchronous</b> producer responsible for transmitting {@link EventData} to a specific Event Hub, grouped
 * together in batches. Depending on the {@link CreateBatchOptions options} specified when creating an {@link
 * EventDataBatch}, the events may be automatically routed to an available partition or specific to a partition.
 *
 * <p>
 * Allowing automatic routing of partitions is recommended when:
 * <ul>
 * <li>The sending of events needs to be highly available.</li>
 * <li>The event data should be evenly distributed among all available partitions.</li>
 * </ul>
 *
 * <p>
 * If no partition id is specified, the following rules are used for automatically selecting one:
 * <ol>
 * <li>Distribute the events equally amongst all available partitions using a round-robin approach.</li>
 * <li>If a partition becomes unavailable, the Event Hubs service will automatically detect it and forward the
 * message to another available partition.</li>
 * </ol>
 *
 * <p><strong>Create a producer and publish events to any partition</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch -->
 * <pre>
 * &#47;&#47; The required parameter is a way to authenticate with Event Hubs using credentials.
 * &#47;&#47; The connectionString provides a way to authenticate with Event Hub.
 * EventHubProducerAsyncClient producer = new EventHubClientBuilder&#40;&#41;
 *     .connectionString&#40;
 *         &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;,
 *         &quot;event-hub-name&quot;&#41;
 *     .buildAsyncProducerClient&#40;&#41;;
 *
 * &#47;&#47; Creating a batch without options set, will allow for automatic routing of events to any partition.
 * producer.createBatch&#40;&#41;.flatMap&#40;batch -&gt; &#123;
 *     batch.tryAdd&#40;new EventData&#40;&quot;test-event-1&quot;&#41;&#41;;
 *     batch.tryAdd&#40;new EventData&#40;&quot;test-event-2&quot;&#41;&#41;;
 *     return producer.send&#40;batch&#41;;
 * &#125;&#41;.subscribe&#40;unused -&gt; &#123; &#125;,
 *     error -&gt; System.err.println&#40;&quot;Error occurred while sending batch:&quot; + error&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Send complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch -->
 *
 * <p><strong>Publish events to partition "foo"</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionId -->
 * <pre>
 * &#47;&#47; Creating a batch with partitionId set will route all events in that batch to partition `foo`.
 * CreateBatchOptions options = new CreateBatchOptions&#40;&#41;.setPartitionId&#40;&quot;foo&quot;&#41;;
 * producer.createBatch&#40;options&#41;.flatMap&#40;batch -&gt; &#123;
 *     batch.tryAdd&#40;new EventData&#40;&quot;test-event-1&quot;&#41;&#41;;
 *     batch.tryAdd&#40;new EventData&#40;&quot;test-event-2&quot;&#41;&#41;;
 *     return producer.send&#40;batch&#41;;
 * &#125;&#41;.subscribe&#40;unused -&gt; &#123; &#125;,
 *     error -&gt; System.err.println&#40;&quot;Error occurred while sending batch:&quot; + error&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Send complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionId -->
 *
 * <p><strong>Publish events to the same partition, grouped together using partition key</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionKey -->
 * <pre>
 * &#47;&#47; Creating a batch with partitionKey set will tell the service to hash the partitionKey and decide which
 * &#47;&#47; partition to send the events to. Events with the same partitionKey are always routed to the same partition.
 * CreateBatchOptions options = new CreateBatchOptions&#40;&#41;.setPartitionKey&#40;&quot;bread&quot;&#41;;
 * producer.createBatch&#40;options&#41;.flatMap&#40;batch -&gt; &#123;
 *     batch.tryAdd&#40;new EventData&#40;&quot;sourdough&quot;&#41;&#41;;
 *     batch.tryAdd&#40;new EventData&#40;&quot;rye&quot;&#41;&#41;;
 *     return producer.send&#40;batch&#41;;
 * &#125;&#41;.subscribe&#40;unused -&gt; &#123; &#125;,
 *     error -&gt; System.err.println&#40;&quot;Error occurred while sending batch:&quot; + error&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Send complete.&quot;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionKey -->
 *
 * <p><strong>Publish events using a size-limited {@link EventDataBatch}</strong></p>
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-int -->
 * <pre>
 * Flux&lt;EventData&gt; telemetryEvents = Flux.just&#40;firstEvent, secondEvent&#41;;
 *
 * &#47;&#47; Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
 * &#47;&#47; In this case, all the batches created with these options are limited to 256 bytes.
 * CreateBatchOptions options = new CreateBatchOptions&#40;&#41;.setMaximumSizeInBytes&#40;256&#41;;
 * AtomicReference&lt;EventDataBatch&gt; currentBatch = new AtomicReference&lt;&gt;&#40;
 *     producer.createBatch&#40;options&#41;.block&#40;&#41;&#41;;
 *
 * &#47;&#47; The sample Flux contains two events, but it could be an infinite stream of telemetry events.
 * telemetryEvents.flatMap&#40;event -&gt; &#123;
 *     final EventDataBatch batch = currentBatch.get&#40;&#41;;
 *     if &#40;batch.tryAdd&#40;event&#41;&#41; &#123;
 *         return Mono.empty&#40;&#41;;
 *     &#125;
 *
 *     return Mono.when&#40;
 *         producer.send&#40;batch&#41;,
 *         producer.createBatch&#40;options&#41;.map&#40;newBatch -&gt; &#123;
 *             currentBatch.set&#40;newBatch&#41;;
 *
 *             &#47;&#47; Add the event that did not fit in the previous batch.
 *             if &#40;!newBatch.tryAdd&#40;event&#41;&#41; &#123;
 *                 throw Exceptions.propagate&#40;new IllegalArgumentException&#40;
 *                     &quot;Event was too large to fit in an empty batch. Max size: &quot; + newBatch.getMaxSizeInBytes&#40;&#41;&#41;&#41;;
 *             &#125;
 *
 *             return newBatch;
 *         &#125;&#41;&#41;;
 * &#125;&#41;.then&#40;&#41;
 *     .doFinally&#40;signal -&gt; &#123;
 *         final EventDataBatch batch = currentBatch.getAndSet&#40;null&#41;;
 *         if &#40;batch != null &amp;&amp; batch.getCount&#40;&#41; &gt; 0&#41; &#123;
 *             producer.send&#40;batch&#41;.block&#40;&#41;;
 *         &#125;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-int -->
 *
 * @see EventHubClientBuilder#buildAsyncProducerClient()
 * @see EventHubProducerClient To synchronously generate events to an Event Hub, see EventHubProducerClient.
 */
@ServiceClient(builder = EventHubClientBuilder.class, isAsync = true)
public class EventHubProducerAsyncClient implements Closeable {
    private static final int MAX_PARTITION_KEY_LENGTH = 128;
    private static final String SENDER_ENTITY_PATH_FORMAT = "%s/Partitions/%s";

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();
    private static final CreateBatchOptions DEFAULT_BATCH_OPTIONS = new CreateBatchOptions();

    private static final ClientLogger LOGGER = new ClientLogger(EventHubProducerAsyncClient.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final EventHubConnectionProcessor connectionProcessor;
    private final AmqpRetryOptions retryOptions;
    private final AmqpRetryPolicy retryPolicy;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final Scheduler scheduler;
    private final boolean isSharedConnection;
    private final Runnable onClientClose;
    private final String identifier;

    private final EventHubsMetricsProvider metricsProvider;

    /**
     * Creates a new instance of this {@link EventHubProducerAsyncClient} that can send messages to a single partition
     * when {@link CreateBatchOptions#getPartitionId()} is not null or an empty string. Otherwise, allows the service to
     * load balance the messages amongst available partitions.
     */
    EventHubProducerAsyncClient(String fullyQualifiedNamespace, String eventHubName,
        EventHubConnectionProcessor connectionProcessor, AmqpRetryOptions retryOptions, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, Scheduler scheduler, boolean isSharedConnection, Runnable onClientClose,
        String identifier, Meter meter) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.onClientClose = Objects.requireNonNull(onClientClose, "'onClientClose' cannot be null.");

        this.retryPolicy = getRetryPolicy(retryOptions);
        this.scheduler = scheduler;
        this.isSharedConnection = isSharedConnection;
        this.identifier = identifier;
        this.metricsProvider = new EventHubsMetricsProvider(meter, fullyQualifiedNamespace, eventHubName, null);
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with.
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
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     * @throws NullPointerException if {@code partitionId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        return connectionProcessor.flatMap(connection -> connection.getManagementNode())
            .flatMap(node -> node.getPartitionProperties(partitionId));
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventDataBatch> createBatch() {
        return createBatch(DEFAULT_BATCH_OPTIONS);
    }

    /**
     * Creates an {@link EventDataBatch} configured with the options specified.
     *
     * @param options A set of options used to configure the {@link EventDataBatch}.
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     * @throws NullPointerException if {@code options} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventDataBatch> createBatch(CreateBatchOptions options) {
        if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }

        final String partitionKey = options.getPartitionKey();
        final String partitionId = options.getPartitionId();
        final int batchMaxSize = options.getMaximumSizeInBytes();

        if (!CoreUtils.isNullOrEmpty(partitionKey)
            && !CoreUtils.isNullOrEmpty(partitionId)) {
            return monoError(LOGGER, new IllegalArgumentException(String.format(Locale.US,
                "CreateBatchOptions.getPartitionKey() and CreateBatchOptions.getPartitionId() are both set. "
                    + "Only one or the other can be used. partitionKey: '%s'. partitionId: '%s'",
                partitionKey, partitionId)));
        } else if (!CoreUtils.isNullOrEmpty(partitionKey)
            && partitionKey.length() > MAX_PARTITION_KEY_LENGTH) {
            return monoError(LOGGER, new IllegalArgumentException(String.format(Locale.US,
                "Partition key '%s' exceeds the maximum allowed length: '%s'.", partitionKey,
                MAX_PARTITION_KEY_LENGTH)));
        }

        return getSendLink(partitionId)
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int maximumLinkSize = size > 0
                        ? size
                        : MAX_MESSAGE_LENGTH_BYTES;

                    if (batchMaxSize > maximumLinkSize) {
                        return monoError(LOGGER,
                            new IllegalArgumentException(String.format(Locale.US,
                                "BatchOptions.maximumSizeInBytes (%s bytes) is larger than the link size (%s bytes).",
                                batchMaxSize, maximumLinkSize)));
                    }

                    final int batchSize = batchMaxSize > 0
                        ? batchMaxSize
                        : maximumLinkSize;

                    return Mono.just(new EventDataBatch(batchSize, partitionId, partitionKey, link::getErrorContext,
                        tracerProvider, link.getEntityPath(), link.getHostname()));
                }));
    }

    /**
     * Sends a single event to the associated Event Hub. If the size of the single event exceeds the maximum size
     * allowed, an exception will be triggered and the send will fail.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     * @return A {@link Mono} that completes when the event is pushed to the service.
     */
    Mono<Void> send(EventData event) {
        if (event == null) {
            return monoError(LOGGER, new NullPointerException("'event' cannot be null."));
        }

        return send(Flux.just(event));
    }

    /**
     * Sends a single event to the associated Event Hub with the send options. If the size of the single event exceeds
     * the maximum size allowed, an exception will be triggered and the send will fail.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     * @param options The set of options to consider when sending this event.
     * @return A {@link Mono} that completes when the event is pushed to the service.
     */
    Mono<Void> send(EventData event, SendOptions options) {
        if (event == null) {
            return monoError(LOGGER, new NullPointerException("'event' cannot be null."));
        } else if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }

        return send(Flux.just(event), options);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * <!-- src_embed com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable -->
     * <pre>
     * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;maple&quot;&#41;, new EventData&#40;&quot;aspen&quot;&#41;,
     *     new EventData&#40;&quot;oak&quot;&#41;&#41;;
     * producer
     *     .send&#40;events&#41;
     *     .subscribe&#40;unused -&gt; &#123; &#125;,
     *         error -&gt; System.err.println&#40;&quot;Error occurred while sending events:&quot; + error&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Send complete.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable -->
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> send(Iterable<EventData> events) {
        if (events == null) {
            return monoError(LOGGER, new NullPointerException("'events' cannot be null."));
        }

        return send(Flux.fromIterable(events));
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * <!-- src_embed com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable-SendOptions -->
     * <pre>
     * List&lt;EventData&gt; events = Arrays.asList&#40;new EventData&#40;&quot;Melbourne&quot;&#41;, new EventData&#40;&quot;London&quot;&#41;,
     *     new EventData&#40;&quot;New York&quot;&#41;&#41;;
     * SendOptions sendOptions = new SendOptions&#40;&#41;.setPartitionKey&#40;&quot;cities&quot;&#41;;
     * producer
     *     .send&#40;events, sendOptions&#41;
     *     .subscribe&#40;unused -&gt; &#123; &#125;,
     *         error -&gt; System.err.println&#40;&quot;Error occurred while sending events:&quot; + error&#41;,
     *         &#40;&#41; -&gt; System.out.println&#40;&quot;Send complete.&quot;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable-SendOptions -->
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     * @throws AmqpException if the size of {@code events} exceed the maximum size of a single batch.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> send(Iterable<EventData> events, SendOptions options) {
        if (events == null) {
            return monoError(LOGGER, new NullPointerException("'events' cannot be null."));
        } else if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }

        return send(Flux.fromIterable(events), options);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    Mono<Void> send(Flux<EventData> events) {
        if (events == null) {
            return monoError(LOGGER, new NullPointerException("'events' cannot be null."));
        }

        return send(events, DEFAULT_SEND_OPTIONS);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    Mono<Void> send(Flux<EventData> events, SendOptions options) {
        if (events == null) {
            return monoError(LOGGER, new NullPointerException("'events' cannot be null."));
        } else if (options == null) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }

        return sendInternal(events, options).publishOn(scheduler);
    }

    /**
     * Sends the batch to the associated Event Hub.
     *
     * @param batch The batch to send to the service.
     * @return A {@link Mono} that completes when the batch is pushed to the service.
     * @throws NullPointerException if {@code batch} is {@code null}.
     * @see EventHubProducerAsyncClient#createBatch()
     * @see EventHubProducerAsyncClient#createBatch(CreateBatchOptions)
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> send(EventDataBatch batch) {
        if (batch == null) {
            return monoError(LOGGER, new NullPointerException("'batch' cannot be null."));
        } else if (batch.getEvents().isEmpty()) {
            LOGGER.warning(Messages.CANNOT_SEND_EVENT_BATCH_EMPTY);
            return Mono.empty();
        }

        if (!CoreUtils.isNullOrEmpty(batch.getPartitionId())) {
            LOGGER.atVerbose()
                .addKeyValue("size", batch.getCount())
                .addKeyValue(PARTITION_ID_KEY, batch.getPartitionId())
                .log("Sending batch.");
        } else if (!CoreUtils.isNullOrEmpty(batch.getPartitionKey())) {
            LOGGER.atVerbose()
                .addKeyValue("size", batch.getCount())
                .addKeyValue(PARTITION_KEY_KEY, batch.getPartitionKey())
                .log("Sending batch.");
        } else {
            LOGGER.atVerbose()
                .addKeyValue("size", batch.getCount())
                .log("Sending batch to be distributed round-robin in service.");
        }

        final String partitionKey = batch.getPartitionKey();
        final boolean isTracingEnabled = tracerProvider.isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;

        Context sharedContext = null;
        final List<Message> messages = new ArrayList<>();

        for (int i = 0; i < batch.getEvents().size(); i++) {
            final EventData event = batch.getEvents().get(i);
            if (isTracingEnabled) {
                if (i == 0) {
                    sharedContext = event.getContext()
                        .addData(AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE)
                        .addData(ENTITY_PATH_KEY, eventHubName)
                        .addData(HOST_NAME_KEY, fullyQualifiedNamespace);

                    sharedContext = tracerProvider.getSharedSpanBuilder(ClientConstants.AZ_TRACING_SERVICE_NAME, sharedContext);
                    tracerProvider.addSpanLinks(sharedContext);
                } else {
                    // TODO (lmolkova) we need better addSpanLinks - https://github.com/Azure/azure-sdk-for-java/issues/28953
                    Object eventSpanContext = event.getContext().getData(SPAN_CONTEXT_KEY).orElse(Context.NONE);
                    tracerProvider.addSpanLinks(sharedContext.addData(SPAN_CONTEXT_KEY, eventSpanContext));
                }
            }

            final Message message = messageSerializer.serialize(event);

            if (!CoreUtils.isNullOrEmpty(partitionKey)) {
                final MessageAnnotations messageAnnotations = message.getMessageAnnotations() == null
                    ? new MessageAnnotations(new HashMap<>())
                    : message.getMessageAnnotations();
                messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
                message.setMessageAnnotations(messageAnnotations);
            }
            messages.add(message);
        }

        if (isTracingEnabled) {
            // Start send span and store updated context
            parentContext.set(tracerProvider.startSpan(AZ_TRACING_SERVICE_NAME, sharedContext, ProcessKind.SEND));
        }

        final Mono<Void> sendMessage = getSendLink(batch.getPartitionId())
            .flatMap(link -> messages.size() == 1
                ? link.send(messages.get(0))
                : link.send(messages));

        return withRetry(sendMessage, retryOptions,
            String.format("partitionId[%s]: Sending messages timed out.", batch.getPartitionId()))
            .publishOn(scheduler)
            .doOnEach(signal -> {
                Context context = isTracingEnabled ? parentContext.get() : Context.NONE;
                metricsProvider.reportBatchSend(batch, batch.getPartitionId(), signal.getThrowable(), context);
                if (isTracingEnabled) {
                    tracerProvider.endSpan(context, signal);
                }
            });
    }

    private Mono<Void> sendInternal(Flux<EventData> events, SendOptions options) {
        final String partitionKey = options.getPartitionKey();
        final String partitionId = options.getPartitionId();

        if (!CoreUtils.isNullOrEmpty(partitionKey)
            && !CoreUtils.isNullOrEmpty(partitionId)) {
            return monoError(LOGGER, new IllegalArgumentException(String.format(Locale.US,
                "SendOptions.getPartitionKey() and SendOptions.getPartitionId() are both set. Only one or the"
                    + " other can be used. partitionKey: '%s'. partitionId: '%s'",
                partitionKey, partitionId)));
        }

        return getSendLink(options.getPartitionId())
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final CreateBatchOptions batchOptions = new CreateBatchOptions()
                        .setPartitionKey(options.getPartitionKey())
                        .setPartitionId(options.getPartitionId())
                        .setMaximumSizeInBytes(batchSize);
                    return events.collect(new EventDataCollector(batchOptions, 1, link::getErrorContext,
                        tracerProvider, link.getEntityPath(), link.getHostname()));
                })
                .flatMap(list -> sendInternal(Flux.fromIterable(list))));
    }

    private Mono<Void> sendInternal(Flux<EventDataBatch> eventBatches) {
        return eventBatches
            .flatMap(this::send)
            .then()
            .doOnError(error -> {
                LOGGER.error(Messages.ERROR_SENDING_BATCH, error);
            });
    }

    private String getEntityPath(String partitionId) {
        return CoreUtils.isNullOrEmpty(partitionId)
            ? eventHubName
            : String.format(Locale.US, SENDER_ENTITY_PATH_FORMAT, eventHubName, partitionId);
    }

    private Mono<AmqpSendLink> getSendLink(String partitionId) {
        final String entityPath = getEntityPath(partitionId);
        final String linkName = entityPath;

        return connectionProcessor
            .flatMap(connection -> connection.createSendLink(linkName, entityPath, retryOptions, identifier));
    }

    /**
     * Disposes of the {@link EventHubProducerAsyncClient}. If the client had a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        if (isSharedConnection) {
            onClientClose.run();
        } else {
            connectionProcessor.dispose();
        }
    }

    /**
     * Gets the client identifier.
     *
     * @return The unique identifier string for current client.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Collects EventData into EventDataBatch to send to Event Hubs. If {@code maxNumberOfBatches} is {@code null} then
     * it'll collect as many batches as possible. Otherwise, if there are more events than can fit into {@code
     * maxNumberOfBatches}, then the collector throws a {@link AmqpException} with {@link
     * AmqpErrorCondition#LINK_PAYLOAD_SIZE_EXCEEDED}.
     */
    private static class EventDataCollector implements Collector<EventData, List<EventDataBatch>,
        List<EventDataBatch>> {
        private final String partitionKey;
        private final String partitionId;
        private final int maxMessageSize;
        private final Integer maxNumberOfBatches;
        private final ErrorContextProvider contextProvider;
        private final TracerProvider tracerProvider;
        private final String entityPath;
        private final String hostname;

        private volatile EventDataBatch currentBatch;

        EventDataCollector(CreateBatchOptions options, Integer maxNumberOfBatches, ErrorContextProvider contextProvider,
            TracerProvider tracerProvider, String entityPath, String hostname) {
            this.maxNumberOfBatches = maxNumberOfBatches;
            this.maxMessageSize = options.getMaximumSizeInBytes() > 0
                ? options.getMaximumSizeInBytes()
                : MAX_MESSAGE_LENGTH_BYTES;
            this.partitionKey = options.getPartitionKey();
            this.partitionId = options.getPartitionId();
            this.contextProvider = contextProvider;
            this.tracerProvider = tracerProvider;
            this.entityPath = entityPath;
            this.hostname = hostname;

            currentBatch = new EventDataBatch(maxMessageSize, partitionId, partitionKey, contextProvider,
                tracerProvider, entityPath, hostname);
        }

        @Override
        public Supplier<List<EventDataBatch>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<EventDataBatch>, EventData> accumulator() {
            return (list, event) -> {
                EventDataBatch batch = currentBatch;
                if (batch.tryAdd(event)) {
                    return;
                }

                if (maxNumberOfBatches != null && list.size() == maxNumberOfBatches) {
                    final String message = String.format(Locale.US,
                        Messages.EVENT_DATA_DOES_NOT_FIT, maxNumberOfBatches);

                    throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message,
                        contextProvider.getErrorContext());
                }

                currentBatch = new EventDataBatch(maxMessageSize, partitionId, partitionKey, contextProvider,
                    tracerProvider, entityPath, hostname);
                currentBatch.tryAdd(event);
                list.add(batch);
            };
        }

        @Override
        public BinaryOperator<List<EventDataBatch>> combiner() {
            return (existing, another) -> {
                existing.addAll(another);
                return existing;
            };
        }

        @Override
        public Function<List<EventDataBatch>, List<EventDataBatch>> finisher() {
            return list -> {
                EventDataBatch batch = currentBatch;
                currentBatch = null;

                if (batch != null) {
                    list.add(batch);
                }

                return list;
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }
}
