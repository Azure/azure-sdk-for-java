// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.InitializationContext;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * EventProcessorClient}. Calling {@link #buildEventProcessorClient()} constructs a new instance of {@link
 * EventProcessorClient}.
 *
 * <p>
 * To create an instance of {@link EventProcessorClient}, the <b>following fields are required</b>:
 *
 * <ul>
 * <li>{@link #consumerGroup(String) Consumer group name}.</li>
 * <li>{@link CheckpointStore} - An implementation of CheckpointStore that stores checkpoint and
 * partition ownership information to enable load balancing and checkpointing processed events.</li>
 * <li>{@link #processEvent(Consumer) processEvent} or
 * {@link #processEventBatch(Consumer, int, Duration) processEventBatch} - A callback that processes events received
 * from the Event Hub.</li>
 * <li>{@link #processError(Consumer) processError} - A callback that handles errors that may occur while running the
 * EventProcessorClient.</li>
 * <li>Credentials to perform operations against Azure Event Hubs. They can be set by using one of the following
 * methods:
 * <ul>
 * <li>{@link #connectionString(String) connectionString(String)} with a connection string to a specific Event Hub.
 * </li>
 * <li>{@link #connectionString(String, String) connectionString(String, String)} with an Event Hub <i>namespace</i>
 * connection string and the Event Hub name.</li>
 * <li>{@link #credential(String, String, TokenCredential) credential(String, String, TokenCredential)} with the fully
 * qualified namespace, Event Hub name, and a set of credentials authorized to use the Event Hub.
 * </li>
 * </ul>
 * </li>
 * </ul>
 *
 * <p><strong>Creating an {@link EventProcessorClient}</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventprocessorclientbuilder.instantiation}
 *
 * @see EventProcessorClient
 * @see EventHubConsumerClient
 * @see EventHubConsumerAsyncClient
 */
@ServiceClientBuilder(serviceClients = EventProcessorClient.class)
public class EventProcessorClientBuilder {

    public static final Duration DEFAULT_LOAD_BALANCING_UPDATE_INTERVAL = Duration.ofSeconds(10);
    public static final int DEFAULT_OWNERSHIP_EXPIRATION_FACTOR = 6;
    private final ClientLogger logger = new ClientLogger(EventProcessorClientBuilder.class);

    private final EventHubClientBuilder eventHubClientBuilder;
    private String consumerGroup;
    private CheckpointStore checkpointStore;
    private Consumer<EventContext> processEvent;
    private Consumer<EventBatchContext> processEventBatch;
    private Consumer<ErrorContext> processError;
    private Consumer<InitializationContext> processPartitionInitialization;
    private Consumer<CloseContext> processPartitionClose;
    private boolean trackLastEnqueuedEventProperties;
    private Map<String, EventPosition> initialPartitionEventPosition = new HashMap<>();
    private int maxBatchSize = 1; // setting this to 1 by default
    private Duration maxWaitTime;
    private Duration loadBalancingUpdateInterval;
    private Duration partitionOwnershipExpirationInterval;
    private LoadBalancingStrategy loadBalancingStrategy = LoadBalancingStrategy.BALANCED;

    /**
     * Creates a new instance of {@link EventProcessorClientBuilder}.
     */
    public EventProcessorClientBuilder() {
        eventHubClientBuilder = new EventHubClientBuilder();
    }

    /**
     * Sets the credential information given a connection string to the Event Hub instance.
     *
     * <p>
     * If the connection string is copied from the Event Hubs namespace, it will likely not contain the name to the
     * desired Event Hub, which is needed. In this case, the name can be added manually by adding {@literal
     * "EntityPath=EVENT_HUB_NAME"} to the end of the connection string. For example, "EntityPath=telemetry-hub".
     * </p>
     *
     * <p>
     * If you have defined a shared access policy directly on the Event Hub itself, then copying the connection string
     * from that Event Hub will result in a connection string that contains the name.
     * </p>
     *
     * @param connectionString The connection string to use for connecting to the Event Hub instance. It is expected
     * that the Event Hub name and the shared access key properties are contained in this connection string.
     * @return The updated {@link EventProcessorClientBuilder} object.
     * @throws NullPointerException if {@code connectionString} is {@code null}.
     * @throws IllegalArgumentException if {@code connectionString} is empty. Or, the {@code connectionString} does not
     * contain the "EntityPath" key, which is the name of the Event Hub instance.
     * @throws AzureException If the shared access signature token credential could not be created using the connection
     * string.
     */
    public EventProcessorClientBuilder connectionString(String connectionString) {
        eventHubClientBuilder.connectionString(connectionString);
        return this;
    }

    /**
     * Sets the credential information given a connection string to the Event Hubs namespace and name to a specific
     * Event Hub instance.
     *
     * @param connectionString The connection string to use for connecting to the Event Hubs namespace; it is expected
     * that the shared access key properties are contained in this connection string, but not the Event Hub name.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @return The updated {@link EventProcessorClientBuilder} object.
     * @throws NullPointerException if {@code connectionString} or {@code eventHubName} is null.
     * @throws IllegalArgumentException if {@code connectionString} or {@code eventHubName} is an empty string. Or, if
     * the {@code connectionString} contains the Event Hub name.
     * @throws AzureException If the shared access signature token credential could not be created using the connection
     * string.
     */
    public EventProcessorClientBuilder connectionString(String connectionString, String eventHubName) {
        eventHubClientBuilder.connectionString(connectionString, eventHubName);
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * If not specified, the default configuration store is used to configure the {@link EventHubAsyncClient}. Use
     * {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to configure the {@link EventHubAsyncClient}.
     * @return The updated {@link EventProcessorClientBuilder} object.
     */
    public EventProcessorClientBuilder configuration(Configuration configuration) {
        eventHubClientBuilder.configuration(configuration);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     * similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The token credential to use for authorization. Access controls may be specified by the Event
     * Hubs namespace or the requested Event Hub, depending on Azure configuration.
     * @return The updated {@link EventProcessorClientBuilder} object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, {@code credentials} is
     * null.
     */
    public EventProcessorClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        TokenCredential credential) {
        eventHubClientBuilder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The shared access name and key credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     *
     * @return The updated {@link EventProcessorClientBuilder} object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty
     *     string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, {@code credentials} is
     *     null.
     */
    public EventProcessorClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        AzureNamedKeyCredential credential) {
        eventHubClientBuilder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The shared access signature credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     *
     * @return The updated {@link EventProcessorClientBuilder} object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty
     *     string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, {@code credentials} is
     *     null.
     */
    public EventProcessorClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        AzureSasCredential credential) {
        eventHubClientBuilder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets a custom endpoint address when connecting to the Event Hubs service. This can be useful when your network
     * does not allow connecting to the standard Azure Event Hubs endpoint address, but does allow connecting through
     * an intermediary. For example: {@literal https://my.custom.endpoint.com:55300}.
     * <p>
     * If no port is specified, the default port for the {@link #transportType(AmqpTransportType) transport type} is
     * used.
     *
     * @param customEndpointAddress The custom endpoint address.
     * @return The updated {@link EventProcessorClientBuilder} object.
     * @throws IllegalArgumentException if {@code customEndpointAddress} cannot be parsed into a valid {@link URL}.
     */
    public EventProcessorClientBuilder customEndpointAddress(String customEndpointAddress) {
        eventHubClientBuilder.customEndpointAddress(customEndpointAddress);
        return this;
    }

    /**
     * Sets the proxy configuration to use for {@link EventHubAsyncClient}. When a proxy is configured, {@link
     * AmqpTransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyOptions The proxy options to use.
     * @return The updated {@link EventProcessorClientBuilder} object.
     */
    public EventProcessorClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        eventHubClientBuilder.proxyOptions(proxyOptions);
        return this;
    }

    /**
     * Sets the transport type by which all the communication with Azure Event Hubs occurs. Default value is {@link
     * AmqpTransportType#AMQP}.
     *
     * @param transport The transport type to use.
     * @return The updated {@link EventProcessorClientBuilder} object.
     */
    public EventProcessorClientBuilder transportType(AmqpTransportType transport) {
        eventHubClientBuilder.transportType(transport);
        return this;
    }

    /**
     * Sets the retry policy for {@link EventHubAsyncClient}. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry policy to use.
     * @return The updated {@link EventProcessorClientBuilder} object.
     */
    public EventProcessorClientBuilder retry(AmqpRetryOptions retryOptions) {
        eventHubClientBuilder.retry(retryOptions);
        return this;
    }

    /**
     * Sets the client options for the processor client. The application id set on the client options will be used
     * for tracing. The headers set on {@code ClientOptions} are currently not used but can be used in later releases
     * to add to AMQP message.
     *
     * @param clientOptions The client options.
     * @return The updated {@link EventProcessorClientBuilder} object.
     */
    public EventProcessorClientBuilder clientOptions(ClientOptions clientOptions) {
        eventHubClientBuilder.clientOptions(clientOptions);
        return this;
    }

    /**
     * Sets the consumer group name from which the {@link EventProcessorClient} should consume events.
     *
     * @param consumerGroup The consumer group name this {@link EventProcessorClient} should consume events.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     * @throws NullPointerException if {@code consumerGroup} is {@code null}.
     */
    public EventProcessorClientBuilder consumerGroup(String consumerGroup) {
        this.consumerGroup = Objects.requireNonNull(consumerGroup, "'consumerGroup' cannot be null");
        return this;
    }

    /**
     * Sets the {@link CheckpointStore} the {@link EventProcessorClient} will use for storing partition ownership and
     * checkpoint information.
     *
     * <p>
     * Users can, optionally, provide their own implementation of {@link CheckpointStore} which will store ownership and
     * checkpoint information.
     * </p>
     *
     * @param checkpointStore Implementation of {@link CheckpointStore}.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     * @throws NullPointerException if {@code checkpointStore} is {@code null}.
     */
    public EventProcessorClientBuilder checkpointStore(CheckpointStore checkpointStore) {
        this.checkpointStore = Objects.requireNonNull(checkpointStore, "'checkpointStore' cannot be null");
        return this;
    }

    /**
     * The time interval between load balancing update cycles. This is also generally the interval at which ownership
     * of partitions are renewed. By default, this interval is set to 10 seconds.
     *
     * @param loadBalancingUpdateInterval The time duration between load balancing update cycles.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     * @throws NullPointerException if {@code loadBalancingUpdateInterval} is {@code null}.
     * @throws IllegalArgumentException if {@code loadBalancingUpdateInterval} is zero or a negative duration.
     */
    public EventProcessorClientBuilder loadBalancingUpdateInterval(Duration loadBalancingUpdateInterval) {
        Objects.requireNonNull(loadBalancingUpdateInterval, "'loadBalancingUpdateInterval' cannot be null");
        if (loadBalancingUpdateInterval.isZero() || loadBalancingUpdateInterval.isNegative()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'loadBalancingUpdateInterval' "
                + "should be a positive duration"));
        }
        this.loadBalancingUpdateInterval = loadBalancingUpdateInterval;
        return this;
    }

    /**
     * The time duration after which the ownership of partition expires if it's not renewed by the owning processor
     * instance. This is the duration that this processor instance will wait before taking over the ownership of
     * partitions previously owned by an inactive processor. By default, this duration is set to a minute.
     *
     * @param partitionOwnershipExpirationInterval The time duration after which the ownership of partition expires.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     * @throws NullPointerException if {@code partitionOwnershipExpirationInterval} is {@code null}.
     * @throws IllegalArgumentException if {@code partitionOwnershipExpirationInterval} is zero or a negative duration.
     */
    public EventProcessorClientBuilder partitionOwnershipExpirationInterval(
        Duration partitionOwnershipExpirationInterval) {
        Objects.requireNonNull(partitionOwnershipExpirationInterval, "'partitionOwnershipExpirationInterval' cannot "
            + "be null");
        if (partitionOwnershipExpirationInterval.isZero() || partitionOwnershipExpirationInterval.isNegative()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'partitionOwnershipExpirationInterval' "
                + "should be a positive duration"));
        }
        this.partitionOwnershipExpirationInterval = partitionOwnershipExpirationInterval;
        return this;
    }

    /**
     * The {@link LoadBalancingStrategy} the {@link EventProcessorClient event processor} will use for claiming
     * partition ownership. By default, a {@link LoadBalancingStrategy#BALANCED Balanced} approach will be used.
     *
     * @param loadBalancingStrategy The {@link LoadBalancingStrategy} to use.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     * @throws NullPointerException if {@code loadBalancingStrategy} is {@code null}.
     */
    public EventProcessorClientBuilder loadBalancingStrategy(LoadBalancingStrategy loadBalancingStrategy) {
        this.loadBalancingStrategy = Objects.requireNonNull(loadBalancingStrategy, "'loadBalancingStrategy' cannot be"
            + " null");
        return this;
    }

    /**
     * The function that is called for each event received by this {@link EventProcessorClient}. The input contains the
     * partition context and the event data.
     *
     * @param processEvent The callback that's called when an event is received by this {@link EventProcessorClient}.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     * @throws NullPointerException if {@code processEvent} is {@code null}.
     */
    public EventProcessorClientBuilder processEvent(Consumer<EventContext> processEvent) {
        return this.processEvent(processEvent, null);
    }

    /**
     * The function that is called for each event received by this {@link EventProcessorClient}. The input contains the
     * partition context and the event data. If the max wait time is set, the receive will wait for that duration to
     * receive an event and if is no event received, the consumer will be invoked with a null event data.
     *
     * @param processEvent The callback that's called when an event is received by this {@link EventProcessorClient} or
     * when the max wait duration has expired.
     * @param maxWaitTime The max time duration to wait to receive an event before invoking this handler.
     * @return The updated {@link EventProcessorClient} instance.
     * @throws NullPointerException if {@code processEvent} is {@code null}.
     */
    public EventProcessorClientBuilder processEvent(Consumer<EventContext> processEvent, Duration maxWaitTime) {
        this.processEvent = Objects.requireNonNull(processEvent, "'processEvent' cannot be null");
        if (maxWaitTime != null && maxWaitTime.isZero()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'maxWaitTime' cannot be 0"));
        }
        this.maxWaitTime = maxWaitTime;
        return this;
    }

    /**
     * The function that is called for each event received by this {@link EventProcessorClient}. The input contains the
     * partition context and the event data. If the max wait time is set, the receive will wait for that duration to
     * receive an event and if is no event received, the consumer will be invoked with a null event data.
     *
     * @param processEventBatch The callback that's called when an event is received by this {@link
     * EventProcessorClient} or when the max wait duration has expired.
     * @param maxBatchSize The maximum number of events that will be in the list when this callback is invoked.
     * @return The updated {@link EventProcessorClient} instance.
     * @throws NullPointerException if {@code processEvent} is {@code null}.
     */
    public EventProcessorClientBuilder processEventBatch(Consumer<EventBatchContext> processEventBatch,
        int maxBatchSize) {
        return this.processEventBatch(processEventBatch, maxBatchSize, null);
    }

    /**
     * The function that is called for each event received by this {@link EventProcessorClient}. The input contains the
     * partition context and the event data. If the max wait time is set, the receive will wait for that duration to
     * receive an event and if is no event received, the consumer will be invoked with a null event data.
     *
     * {@codesnippet com.azure.messaging.eventhubs.eventprocessorclientbuilder.batchreceive}
     *
     * @param processEventBatch The callback that's called when an event is received  or when the max wait duration has
     * expired.
     * @param maxBatchSize The maximum number of events that will be in the list when this callback is invoked.
     * @param maxWaitTime The max time duration to wait to receive a batch of events upto the max batch size before
     * invoking this callback.
     * @return The updated {@link EventProcessorClient} instance.
     * @throws NullPointerException if {@code processEvent} is {@code null}.
     */
    public EventProcessorClientBuilder processEventBatch(Consumer<EventBatchContext> processEventBatch,
        int maxBatchSize, Duration maxWaitTime) {
        if (maxBatchSize <= 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'maxBatchSize' should be greater than 0"));
        }
        if (maxWaitTime != null && maxWaitTime.isZero()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'maxWaitTime' cannot be 0"));
        }
        this.processEventBatch = Objects.requireNonNull(processEventBatch, "'processEventBatch' cannot be null");
        this.maxBatchSize = maxBatchSize;
        this.maxWaitTime = maxWaitTime;

        return this;
    }

    /**
     * The function that is called when an error occurs while processing events. The input contains the partition
     * information where the error happened.
     *
     * @param processError The callback that's called when an error occurs while processing events.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     */
    public EventProcessorClientBuilder processError(Consumer<ErrorContext> processError) {
        this.processError = processError;
        return this;
    }

    /**
     * The function that is called before processing starts for a partition. The input contains the partition
     * information along with a default starting position for processing events that will be used in the case of a
     * checkpoint unavailable in {@link CheckpointStore}. Users can update this position if a different starting
     * position is preferred.
     *
     * @param initializePartition The callback that's called before processing starts for a partition
     * @return The updated {@link EventProcessorClientBuilder} instance.
     */
    public EventProcessorClientBuilder processPartitionInitialization(
        Consumer<InitializationContext> initializePartition) {
        this.processPartitionInitialization = initializePartition;
        return this;
    }

    /**
     * The function that is called when a processing for a partition stops. The input contains the partition information
     * along with the reason for stopping the event processing for this partition.
     *
     * @param closePartition The callback that's called after processing for a partition stops.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     */
    public EventProcessorClientBuilder processPartitionClose(Consumer<CloseContext> closePartition) {
        this.processPartitionClose = closePartition;
        return this;
    }

    /**
     * Sets whether or not the event processor should request information on the last enqueued event on its associated
     * partition, and track that information as events are received.
     *
     * <p>When information about the partition's last enqueued event is being tracked, each event received from the
     * Event Hubs service will carry metadata about the partition that it otherwise would not. This results in a small
     * amount of additional network bandwidth consumption that is generally a favorable trade-off when considered
     * against periodically making requests for partition properties using the Event Hub client.</p>
     *
     * @param trackLastEnqueuedEventProperties {@code true} if the resulting events will keep track of the last enqueued
     * information for that partition; {@code false} otherwise.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     */
    public EventProcessorClientBuilder trackLastEnqueuedEventProperties(boolean trackLastEnqueuedEventProperties) {
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        return this;
    }

    /**
     * Sets the map containing the event position to use for each partition if a checkpoint for the partition does not
     * exist in {@link CheckpointStore}. This map is keyed off of the partition id. If there is no checkpoint in {@link
     * CheckpointStore} and there is no entry in this map, the processing of the partition will start from {@link
     * EventPosition#latest() latest} position.
     *
     * @param initialPartitionEventPosition Map of initial event positions for partition ids.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     */
    public EventProcessorClientBuilder initialPartitionEventPosition(
        Map<String, EventPosition> initialPartitionEventPosition) {

        this.initialPartitionEventPosition = Objects.requireNonNull(initialPartitionEventPosition,
            "'initialPartitionEventPosition' cannot be null.");
        return this;
    }

    /**
     * This will create a new {@link EventProcessorClient} configured with the options set in this builder. Each call to
     * this method will return a new instance of {@link EventProcessorClient}.
     *
     * <p>
     * All partitions processed by this {@link EventProcessorClient} will start processing from {@link
     * EventPosition#earliest() earliest} available event in the respective partitions.
     * </p>
     *
     * @return A new instance of {@link EventProcessorClient}.
     * @throws NullPointerException if {@code processEvent} or {@code processError} or {@code checkpointStore} or {@code
     * consumerGroup} is {@code null}.
     * @throws IllegalArgumentException if the credentials have not been set using either {@link
     * #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy is specified
     * but the transport type is not {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventProcessorClient buildEventProcessorClient() {
        Objects.requireNonNull(processError, "'processError' cannot be null");
        Objects.requireNonNull(checkpointStore, "'checkpointStore' cannot be null");
        Objects.requireNonNull(consumerGroup, "'consumerGroup' cannot be null");

        if (processEvent == null && processEventBatch == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Either processEvent or processEventBatch "
                + "has to be set"));
        }

        if (processEvent != null && processEventBatch != null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Both processEvent and processEventBatch "
                + "cannot be set"));
        }

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));
        if (loadBalancingUpdateInterval == null) {
            loadBalancingUpdateInterval = DEFAULT_LOAD_BALANCING_UPDATE_INTERVAL;
        }
        if (partitionOwnershipExpirationInterval == null) {
            partitionOwnershipExpirationInterval = loadBalancingUpdateInterval.multipliedBy(
                DEFAULT_OWNERSHIP_EXPIRATION_FACTOR);
        }

        return new EventProcessorClient(eventHubClientBuilder, consumerGroup,
            getPartitionProcessorSupplier(), checkpointStore, trackLastEnqueuedEventProperties, tracerProvider,
            processError, initialPartitionEventPosition, maxBatchSize, maxWaitTime, processEventBatch != null,
            loadBalancingUpdateInterval, partitionOwnershipExpirationInterval, loadBalancingStrategy);
    }

    private Supplier<PartitionProcessor> getPartitionProcessorSupplier() {
        return () -> new PartitionProcessor() {
            @Override
            public void processEvent(EventContext eventContext) {
                if (processEvent != null) {
                    processEvent.accept(eventContext);
                }
            }

            @Override
            public void processEventBatch(EventBatchContext eventBatchContext) {
                if (processEventBatch != null) {
                    processEventBatch.accept(eventBatchContext);
                } else {
                    super.processEventBatch(eventBatchContext);
                }
            }

            @Override
            public void initialize(InitializationContext initializationContext) {
                if (processPartitionInitialization != null) {
                    processPartitionInitialization.accept(initializationContext);
                } else {
                    super.initialize(initializationContext);
                }
            }

            @Override
            public void processError(ErrorContext errorContext) {
                processError.accept(errorContext);
            }

            @Override
            public void close(CloseContext closeContext) {
                if (processPartitionClose != null) {
                    processPartitionClose.accept(closeContext);
                } else {
                    super.close(closeContext);
                }
            }
        };
    }

}
