// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsBatchMessageListener;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventProcessorClientProperties;
import com.azure.spring.cloud.service.listener.MessageListener;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.azure.spring.cloud.service.implementation.converter.EventPositionConverter.EVENT_POSITION_CONVERTER;

/**
 * Event Hub client builder factory, it builds the {@link EventHubClientBuilder} according the configuration context and
 * blob properties.
 */
public class EventProcessorClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<EventProcessorClientBuilder> {

    private final EventProcessorClientProperties eventProcessorClientProperties;
    private final CheckpointStore checkpointStore;
    private final MessageListener<?> messageListener;
    private final EventHubsErrorHandler errorHandler;
    private Consumer<CloseContext> closeContextConsumer;
    private Consumer<InitializationContext> initializationContextConsumer;

    /**
     * Create a {@link EventProcessorClientBuilderFactory} with the {@link EventProcessorClientProperties} and a
     * {@link CheckpointStore} and a {@link MessageListener}.
     * @param eventProcessorClientProperties the properties of the event processor client.
     * @param checkpointStore the checkpoint store.
     * @param listener the listener for event processing.
     * @param errorHandler the error handler.
     */
    public EventProcessorClientBuilderFactory(EventProcessorClientProperties eventProcessorClientProperties,
                                              CheckpointStore checkpointStore,
                                              MessageListener<?> listener,
                                              EventHubsErrorHandler errorHandler) {
        this.eventProcessorClientProperties = eventProcessorClientProperties;
        this.checkpointStore = checkpointStore;
        this.messageListener = listener;
        this.errorHandler = errorHandler;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, ProxyOptions> consumeProxyOptions() {
        return EventProcessorClientBuilder::proxyOptions;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, AmqpTransportType> consumeAmqpTransportType() {
        return EventProcessorClientBuilder::transportType;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, AmqpRetryOptions> consumeAmqpRetryOptions() {
        return EventProcessorClientBuilder::retry;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, ClientOptions> consumeClientOptions() {
        return EventProcessorClientBuilder::clientOptions;
    }

    @Override
    protected EventProcessorClientBuilder createBuilderInstance() {
        return new EventProcessorClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.eventProcessorClientProperties;
    }

    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>

    @Override
    protected void configureService(EventProcessorClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(eventProcessorClientProperties.getConsumerGroup()).to(builder::consumerGroup);
        map.from(eventProcessorClientProperties.getPrefetchCount()).to(builder::prefetchCount);
        map.from(eventProcessorClientProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);
        map.from(eventProcessorClientProperties.getTrackLastEnqueuedEventProperties()).to(builder::trackLastEnqueuedEventProperties);
        map.from(eventProcessorClientProperties.getLoadBalancing().getPartitionOwnershipExpirationInterval()).to(builder::partitionOwnershipExpirationInterval);
        map.from(eventProcessorClientProperties.getLoadBalancing().getStrategy()).to(builder::loadBalancingStrategy);
        map.from(eventProcessorClientProperties.getLoadBalancing().getUpdateInterval()).to(builder::loadBalancingUpdateInterval);

        map.from(eventProcessorClientProperties.getInitialPartitionEventPosition()).when(c -> !CollectionUtils.isEmpty(c))
                .to(m -> {
                    Map<String, EventPosition> eventPositionMap = m.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() != null)
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> EVENT_POSITION_CONVERTER.convert(entry.getValue())));
                    builder.initialPartitionEventPosition(eventPositionMap);
                });

        map.from(this.errorHandler).to(builder::processError);
        map.from(this.initializationContextConsumer).to(builder::processPartitionInitialization);
        map.from(this.closeContextConsumer).to(builder::processPartitionClose);

        configureCheckpointStore(builder);
        configureMessageListener(builder);
    }

    //Credentials have not been set. They can be set using:
    // connectionString(String),
    // connectionString(String, String),
    // credentials(String, String, TokenCredential),
    // or setting the environment variable 'AZURE_EVENT_HUBS_CONNECTION_STRING' with a connection string
    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(EventProcessorClientBuilder builder) {
        return Arrays.asList(
            new NamedKeyAuthenticationDescriptor(c -> builder.credential(
                eventProcessorClientProperties.getFullyQualifiedNamespace(), eventProcessorClientProperties.getEventHubName(), c)),
            new SasAuthenticationDescriptor(c -> builder.credential(
                eventProcessorClientProperties.getFullyQualifiedNamespace(), eventProcessorClientProperties.getEventHubName(), c)),
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, c -> builder.credential(
                eventProcessorClientProperties.getFullyQualifiedNamespace(), eventProcessorClientProperties.getEventHubName(), c))
        );
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, Configuration> consumeConfiguration() {
        return EventProcessorClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, tokenCredential) -> builder.credential(eventProcessorClientProperties.getFullyQualifiedNamespace(),
            eventProcessorClientProperties.getEventHubName(),
            tokenCredential);
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, String> consumeConnectionString() {
        return (builder, s) -> builder.connectionString(s, this.eventProcessorClientProperties.getEventHubName());
    }

    private void configureCheckpointStore(EventProcessorClientBuilder builder) {
        builder.checkpointStore(this.checkpointStore);
    }

    private void configureMessageListener(EventProcessorClientBuilder builder) {
        final EventProcessorClientProperties.EventBatch batch = this.eventProcessorClientProperties.getBatch();

        if (messageListener instanceof EventHubsBatchMessageListener) {
            Assert.notNull(batch.getMaxSize(), "Batch max size must be provided");
            builder.processEventBatch(((EventHubsBatchMessageListener) messageListener)::onMessage,
                batch.getMaxSize(), batch.getMaxWaitTime());
        } else if (messageListener instanceof EventHubsRecordMessageListener) {
            builder.processEvent(((EventHubsRecordMessageListener) messageListener)::onMessage);
        } else {
            throw new IllegalArgumentException("Listener must be of one 'EventHubsBatchMessageListener' or "
                + "'EventHubsRecordMessageListener', not " + messageListener.getClass().getName());
        }
    }

    public void setCloseContextConsumer(Consumer<CloseContext> closeContextConsumer) {
        this.closeContextConsumer = closeContextConsumer;
    }

    public void setInitializationContextConsumer(Consumer<InitializationContext> initializationContextConsumer) {
        this.initializationContextConsumer = initializationContextConsumer;
    }
}
