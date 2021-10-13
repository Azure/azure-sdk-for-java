// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhubs.properties.AzureEventHubProperties;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.eventhubs.core.EventProcessorSharedAuthenticationClientBuilder;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Event Hub client builder factory, it builds the {@link EventHubClientBuilder} according the configuration context and
 * blob properties.
 */
public class EventProcessorSharedAuthenticationClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<EventProcessorSharedAuthenticationClientBuilder> {

    private final AzureEventHubProperties eventHubProperties;
    private final CheckpointStore checkpointStore;

    public EventProcessorSharedAuthenticationClientBuilderFactory(AzureEventHubProperties eventHubProperties,
                                                                  CheckpointStore checkpointStore) {
        this.eventHubProperties = eventHubProperties;
        this.checkpointStore = checkpointStore;
    }

    @Override
    protected BiConsumer<EventProcessorSharedAuthenticationClientBuilder, ProxyOptions> consumeProxyOptions() {
        return EventProcessorSharedAuthenticationClientBuilder::proxyOptions;
    }

    @Override
    protected BiConsumer<EventProcessorSharedAuthenticationClientBuilder, AmqpTransportType> consumeAmqpTransportType() {
        return EventProcessorSharedAuthenticationClientBuilder::transportType;
    }

    @Override
    protected BiConsumer<EventProcessorSharedAuthenticationClientBuilder, AmqpRetryOptions> consumeAmqpRetryOptions() {
        return EventProcessorSharedAuthenticationClientBuilder::retry;
    }

    @Override
    protected BiConsumer<EventProcessorSharedAuthenticationClientBuilder, ClientOptions> consumeClientOptions() {
        return EventProcessorSharedAuthenticationClientBuilder::clientOptions;
    }

    @Override
    protected EventProcessorSharedAuthenticationClientBuilder createBuilderInstance() {
        return new EventProcessorSharedAuthenticationClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.eventHubProperties;
    }

    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>

    @Override
    protected void configureService(EventProcessorSharedAuthenticationClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(eventHubProperties.getPrefetchCount()).to(builder::prefetchCount);
        map.from(eventHubProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);
        map.from(eventHubProperties.getProcessor().getConsumerGroup()).to(builder::consumerGroup);
        map.from(eventHubProperties.getProcessor().getTrackLastEnqueuedEventProperties()).to(builder::trackLastEnqueuedEventProperties);
        map.from(eventHubProperties.getProcessor().getPartitionOwnershipExpirationInterval()).to(builder::partitionOwnershipExpirationInterval);
        map.from(eventHubProperties.getProcessor().getInitialPartitionEventPosition()).to(builder::initialPartitionEventPosition);
        map.from(eventHubProperties.getProcessor().getLoadBalancing().getStrategy()).to(builder::loadBalancingStrategy);
        map.from(eventHubProperties.getProcessor().getLoadBalancing().getUpdateInterval()).to(builder::loadBalancingUpdateInterval);

        configureCheckpointStore(builder);
    }


    //Credentials have not been set. They can be set using:
    // connectionString(String),
    // connectionString(String, String),
    // credentials(String, String, TokenCredential),
    // or setting the environment variable 'AZURE_EVENT_HUBS_CONNECTION_STRING' with a connection string
    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(
        EventProcessorSharedAuthenticationClientBuilder builder) {
        return Arrays.asList(
            new NamedKeyAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                                provider.getCredential())),
            new SasAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                           provider.getCredential())),
            new TokenAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                             provider.getCredential()))
        );
    }

    @Override
    protected BiConsumer<EventProcessorSharedAuthenticationClientBuilder, Configuration> consumeConfiguration() {
        return EventProcessorSharedAuthenticationClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<EventProcessorSharedAuthenticationClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, tokenCredential) -> builder.credential(eventHubProperties.getFQDN(),
                                                                tokenCredential);
    }

    @Override
    protected BiConsumer<EventProcessorSharedAuthenticationClientBuilder, String> consumeConnectionString() {
        return EventProcessorSharedAuthenticationClientBuilder::connectionString;
    }

    private void configureCheckpointStore(EventProcessorSharedAuthenticationClientBuilder builder) {
        builder.checkpointStore(this.checkpointStore);
    }

}
