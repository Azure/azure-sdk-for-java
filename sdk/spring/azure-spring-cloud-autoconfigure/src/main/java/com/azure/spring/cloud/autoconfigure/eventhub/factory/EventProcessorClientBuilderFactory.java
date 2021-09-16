// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
import com.azure.spring.integration.eventhub.api.EventProcessorListener;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Event Hub client builder factory, it builds the {@link EventHubClientBuilder} according the configuration context and
 * blob properties.
 */
public class EventProcessorClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<EventProcessorClientBuilder> {

    private final AzureEventHubProperties eventHubProperties;
    private final CheckpointStore checkpointStore;
    private final EventProcessorListener processorListener;

    public EventProcessorClientBuilderFactory(AzureEventHubProperties eventHubProperties,
                                              CheckpointStore checkpointStore,
                                              EventProcessorListener listener) {
        this.eventHubProperties = eventHubProperties;
        this.checkpointStore = checkpointStore;
        this.processorListener = listener;
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
        return null;
    }

    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>

    @Override
    protected void configureService(EventProcessorClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(eventHubProperties.getConsumerGroup()).to(builder::consumerGroup);
        map.from(eventHubProperties.getPrefetchCount()).to(builder::prefetchCount);
        map.from(eventHubProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);
        map.from(eventHubProperties.getProcessor().isTrackLastEnqueuedEventProperties()).to(builder::trackLastEnqueuedEventProperties);
        map.from(eventHubProperties.getProcessor().getPartitionOwnershipExpirationInterval()).to(builder::partitionOwnershipExpirationInterval);
        map.from(eventHubProperties.getProcessor().getInitialPartitionEventPosition()).to(builder::initialPartitionEventPosition);
        map.from(eventHubProperties.getProcessor().getLoadBalancing().getStrategy()).to(builder::loadBalancingStrategy);
        map.from(eventHubProperties.getProcessor().getLoadBalancing().getUpdateInterval()).to(builder::loadBalancingUpdateInterval);

        configureCheckpointStore(builder);
        configureProcessorListener(builder);
    }


    //Credentials have not been set. They can be set using:
    // connectionString(String),
    // connectionString(String, String),
    // credentials(String, String, TokenCredential),
    // or setting the environment variable 'AZURE_EVENT_HUBS_CONNECTION_STRING' with a connection string
    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(EventProcessorClientBuilder builder) {
        return Arrays.asList(
            new NamedKeyAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                                eventHubProperties.getEventHubName(),
                                                                                provider.getCredential())),
            new SasAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                           eventHubProperties.getEventHubName(),
                                                                           provider.getCredential())),
            new TokenAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                             eventHubProperties.getEventHubName(),
                                                                             provider.getCredential()))
        );
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, Configuration> consumeConfiguration() {
        return EventProcessorClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, tokenCredential) -> builder.credential(eventHubProperties.getFQDN(),
                                                                eventHubProperties.getEventHubName(),
                                                                tokenCredential);
    }

    @Override
    protected BiConsumer<EventProcessorClientBuilder, String> consumeConnectionString() {
        return EventProcessorClientBuilder::connectionString;
    }

    private void configureCheckpointStore(EventProcessorClientBuilder builder) {
        builder.checkpointStore(this.checkpointStore);
    }

    private void configureProcessorListener(EventProcessorClientBuilder builder) {
        builder.processError(processorListener::onError);
        builder.processEvent(processorListener::onEvent);
        builder.processPartitionClose(processorListener::onPartitionClose);
        builder.processPartitionInitialization(processorListener::onInitialization);


        final AzureEventHubProperties.Processor.Batch batch = this.eventHubProperties.getProcessor().getBatch();

        if (batch.getMaxWaitTime() != null) {
            builder.processEventBatch(processorListener::onEventBatch, batch.getMaxSize());
        } else {
            builder.processEventBatch(processorListener::onEventBatch, batch.getMaxSize(), batch.getMaxWaitTime());
        }

    }
}
