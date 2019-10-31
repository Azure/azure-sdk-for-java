// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.amqp.models.ProxyConfiguration;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.EventProcessingErrorContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * EventProcessor}. Calling {@link #buildEventProcessor()} constructs a new instance of {@link EventProcessor}.
 *
 * <p>
 * To create an instance of {@link EventProcessor} that processes events with user-provided callback, configure the
 * following fields:
 *
 * <ul>
 * <li>{@link #consumerGroup(String) Consumer group name}.</li>
 * <li>{@link EventProcessorStore} - An implementation of EventProcessorStore that stores checkpoint and
 * partition ownership information to enable load balancing.</li>
 * <li>{@link #processEvent(Function)} - A callback that processes events received from the Event Hub.</li>
 * <li>Credentials -
 *  <strong>Credentials are required</strong> to perform operations against Azure Event Hubs. They can be set by using
 *  one of the following methods:
 *  <ul>
 *  <li>{@link #connectionString(String) connectionString(String)} with a connection string to a specific Event Hub.
 *  </li>
 *  <li>{@link #connectionString(String, String) connectionString(String, String)} with an Event Hub <i>namespace</i>
 *  connection string and the Event Hub name.</li>
 *  <li>{@link #credential(String, String, TokenCredential) credential(String, String, TokenCredential)} with the
 *  fully qualified domain name (FQDN), Event Hub name, and a set of credentials authorized to use the Event Hub.
 *  </li>
 *  </ul>
 *  </li>
 * </ul>
 *
 * <p><strong>Creating an {@link EventProcessor}</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventprocessorbuilder.instantiation}
 *
 * @see EventProcessor
 * @see EventHubConsumerClient
 */
public class EventProcessorBuilder {

    private final ClientLogger logger = new ClientLogger(EventProcessorBuilder.class);

    private final EventHubClientBuilder eventHubClientBuilder;
    private String consumerGroup;
    private EventProcessorStore eventProcessorStore;
    private Function<PartitionEvent, Mono<Void>> processEvent;
    private Function<EventProcessingErrorContext, Mono<Void>> processError;
    private Function<InitializationContext, Mono<Void>> initializePartition;
    private Function<CloseContext, Mono<Void>> closePartition;

    /**
     * Creates a new instance of {@link EventProcessorBuilder}.
     */
    public EventProcessorBuilder() {
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
     * @return The updated {@link EventProcessorBuilder} object.
     * @throws NullPointerException if {@code connectionString} is {@code null}.
     * @throws IllegalArgumentException if {@code connectionString} is empty. Or, the {@code connectionString}
     * does not contain the "EntityPath" key, which is the name of the Event Hub instance.
     * @throws AzureException If the shared access signature token credential could not be created using the connection
     * string.
     */
    public EventProcessorBuilder connectionString(String connectionString) {
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
     * @return The updated {@link EventProcessorBuilder} object.
     * @throws NullPointerException if {@code connectionString} or {@code eventHubName} is null.
     * @throws IllegalArgumentException if {@code connectionString} or {@code eventHubName} is an empty string. Or, if
     * the {@code connectionString} contains the Event Hub name.
     * @throws AzureException If the shared access signature token credential could not be created using the connection
     * string.
     */
    public EventProcessorBuilder connectionString(String connectionString, String eventHubName) {
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
     * @return The updated {@link EventProcessorBuilder} object.
     */
    public EventProcessorBuilder configuration(Configuration configuration) {
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
     * @return The updated {@link EventProcessorBuilder} object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, {@code credentials} is
     * null.
     */
    public EventProcessorBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        TokenCredential credential) {
        eventHubClientBuilder.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    /**
     * Sets the proxy configuration to use for {@link EventHubAsyncClient}. When a proxy is configured, {@link
     * TransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyConfiguration The proxy configuration to use.
     * @return The updated {@link EventProcessorBuilder} object.
     */
    public EventProcessorBuilder proxyConfiguration(ProxyConfiguration proxyConfiguration) {
        eventHubClientBuilder.proxyConfiguration(proxyConfiguration);
        return this;
    }

    /**
     * Sets the scheduler for operations such as connecting to and receiving or sending data to Event Hubs. If none is
     * specified, an elastic pool is used.
     *
     * @param scheduler The scheduler for operations such as connecting to and receiving or sending data to Event Hubs.
     * @return The updated {@link EventProcessorBuilder} object.
     */
    public EventProcessorBuilder scheduler(Scheduler scheduler) {
        eventHubClientBuilder.scheduler(scheduler);
        return this;
    }

    /**
     * Sets the transport type by which all the communication with Azure Event Hubs occurs. Default value is {@link
     * TransportType#AMQP}.
     *
     * @param transport The transport type to use.
     * @return The updated {@link EventProcessorBuilder} object.
     */
    public EventProcessorBuilder transportType(TransportType transport) {
        eventHubClientBuilder.transportType(transport);
        return this;
    }

    /**
     * Sets the retry policy for {@link EventHubAsyncClient}. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry policy to use.
     * @return The updated {@link EventProcessorBuilder} object.
     */
    public EventProcessorBuilder retryOptions(RetryOptions retryOptions) {
        eventHubClientBuilder.retry(retryOptions);
        return this;
    }

    /**
     * Sets the consumer group name from which the {@link EventProcessor} should consume events.
     *
     * @param consumerGroup The consumer group name this {@link EventProcessor} should consume events.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder consumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Sets the {@link EventProcessorStore} the {@link EventProcessor} will use for storing partition ownership and
     * checkpoint information.
     *
     * <p>
     * Users can, optionally, provide their own implementation of {@link EventProcessorStore} which will store ownership
     * and checkpoint information.
     * </p>
     *
     * @param eventProcessorStore Implementation of {@link EventProcessorStore}.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder eventProcessorStore(EventProcessorStore eventProcessorStore) {
        this.eventProcessorStore = eventProcessorStore;
        return this;
    }

    /**
     * The function that is called for each event received by this {@link EventProcessor}. The input contains the
     * partition context and the event data.
     *
     * @param processEvent The function to call when an event is received by this {@link EventProcessor}.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder processEvent(Function<PartitionEvent, Mono<Void>> processEvent) {
        this.processEvent = processEvent;
        return this;
    }

    /**
     * The function that is called when an error occurs while processing events. The input contains the partition
     * information where the error happened.
     *
     * @param processError The function to call when an error occurs while processing events.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder processError(Function<EventProcessingErrorContext, Mono<Void>> processError) {
        this.processError = processError;
        return this;
    }

    /**
     * The function that is called before processing starts for a partition. The input contains the partition
     * information along with a default starting position for processing events that will be used in the case of a
     * checkpoint unavailable in {@link EventProcessorStore}. Users can update this position if a different starting
     * position is preferred.
     *
     * @param initializePartition The function to call before processing starts for a partition
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder initializePartition(
        Function<InitializationContext, Mono<Void>> initializePartition) {
        this.initializePartition = initializePartition;
        return this;
    }

    /**
     * The function that is called when a processing for a partition stops. The input contains the partition information
     * along with the reason for stopping the event processing for this partition.
     *
     * @param closePartition The function to call after processing for a partition stops.
     * @return The updated {@link EventProcessorBuilder} instance.
     */
    public EventProcessorBuilder closePartition(Function<CloseContext, Mono<Void>> closePartition) {
        this.closePartition = closePartition;
        return this;
    }

    /**
     * This will create a new {@link EventProcessor} configured with the options set in this builder. Each call to this
     * method will return a new instance of {@link EventProcessor}.
     *
     * <p>
     * All partitions processed by this {@link EventProcessor} will start processing from {@link
     * EventPosition#earliest() earliest} available event in the respective partitions.
     * </p>
     *
     * @return A new instance of {@link EventProcessor}.
     */
    public EventProcessor buildEventProcessor() {
        Objects.requireNonNull(processEvent, "'processEvent' cannot be null");
        Objects.requireNonNull(eventProcessorStore, "'eventProcessStore' cannot be null");

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));
        return new EventProcessor(eventHubClientBuilder, this.consumerGroup,
            getPartitionProcessorSupplier(), EventPosition.earliest(), eventProcessorStore, tracerProvider);
    }

    private Supplier<PartitionProcessor> getPartitionProcessorSupplier() {
        return () -> new PartitionProcessor() {
            @Override
            public Mono<Void> processEvent(PartitionEvent partitionEvent) {
                return processEvent.apply(partitionEvent);
            }

            @Override
            public Mono<Void> initialize(InitializationContext initializationContext) {
                if (initializePartition != null) {
                    return initializePartition.apply(initializationContext);
                }
                return super.initialize(initializationContext);
            }

            @Override
            public void processError(EventProcessingErrorContext eventProcessingErrorContext) {
                if (processError != null) {
                    processError.apply(eventProcessingErrorContext);
                } else {
                    super.processError(eventProcessingErrorContext);
                }

            }

            @Override
            public Mono<Void> close(CloseContext closeContext) {
                if (closePartition != null) {
                    return closePartition.apply(closeContext);
                }
                return super.close(closeContext);
            }
        };
    }

}
