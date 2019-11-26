// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
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
 * To create an instance of {@link EventProcessorClient} that processes events with user-provided callback, configure
 * the following fields:
 *
 * <ul>
 * <li>{@link #consumerGroup(String) Consumer group name}.</li>
 * <li>{@link CheckpointStore} - An implementation of CheckpointStore that stores checkpoint and
 * partition ownership information to enable load balancing.</li>
 * <li>{@link #processEvent(Consumer)} - A callback that processes events received from the Event Hub.</li>
 * <li>Credentials -
 *  <strong>Credentials are required</strong> to perform operations against Azure Event Hubs. They can be set by using
 *  one of the following methods:
 *  <ul>
 *  <li>{@link #connectionString(String) connectionString(String)} with a connection string to a specific Event Hub.
 *  </li>
 *  <li>{@link #connectionString(String, String) connectionString(String, String)} with an Event Hub <i>namespace</i>
 *  connection string and the Event Hub name.</li>
 *  <li>{@link #credential(String, String, TokenCredential) credential(String, String, TokenCredential)} with the
 *  fully qualified namespace, Event Hub name, and a set of credentials authorized to use the Event Hub.
 *  </li>
 *  </ul>
 *  </li>
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

    private final ClientLogger logger = new ClientLogger(EventProcessorClientBuilder.class);

    private final EventHubClientBuilder eventHubClientBuilder;
    private String consumerGroup;
    private CheckpointStore checkpointStore;
    private Consumer<EventContext> processEvent;
    private Consumer<ErrorContext> processError;
    private Consumer<InitializationContext> processPartitionInitialization;
    private Consumer<CloseContext> processPartitionClose;
    private boolean trackLastEnqueuedEventProperties;

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
     * The function that is called for each event received by this {@link EventProcessorClient}. The input contains the
     * partition context and the event data.
     *
     * @param processEvent The callback that's called when an event is received by this {@link EventProcessorClient}.
     * @return The updated {@link EventProcessorClientBuilder} instance.
     * @throws NullPointerException if {@code processEvent} is {@code null}.
     */
    public EventProcessorClientBuilder processEvent(Consumer<EventContext> processEvent) {
        this.processEvent = Objects.requireNonNull(processEvent, "'processEvent' cannot be null");
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
     * @param trackLastEnqueuedEventProperties {@code true} if the resulting events will keep track of the last
     *     enqueued information for that partition; {@code false} otherwise.
     *
     * @return The updated {@link EventProcessorClientBuilder} instance.
     */
    public EventProcessorClientBuilder trackLastEnqueuedEventProperties(boolean trackLastEnqueuedEventProperties) {
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
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
     * @throws NullPointerException if {@code processEvent} or {@code processError} or {@code checkpointStore} or
     * {@code consumerGroup} is {@code null}.
     * @throws IllegalArgumentException if the credentials have not been set using either {@link
     * #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy is specified
     * but the transport type is not {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventProcessorClient buildEventProcessorClient() {
        Objects.requireNonNull(processEvent, "'processEvent' cannot be null");
        Objects.requireNonNull(processError, "'processError' cannot be null");
        Objects.requireNonNull(checkpointStore, "'checkpointStore' cannot be null");
        Objects.requireNonNull(consumerGroup, "'consumerGroup' cannot be null");

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));
        return new EventProcessorClient(eventHubClientBuilder, this.consumerGroup,
            getPartitionProcessorSupplier(), EventPosition.earliest(), checkpointStore,
            trackLastEnqueuedEventProperties, tracerProvider);
    }

    private Supplier<PartitionProcessor> getPartitionProcessorSupplier() {
        return () -> new PartitionProcessor() {
            @Override
            public void processEvent(EventContext eventContext) {
                processEvent.accept(eventContext);
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
