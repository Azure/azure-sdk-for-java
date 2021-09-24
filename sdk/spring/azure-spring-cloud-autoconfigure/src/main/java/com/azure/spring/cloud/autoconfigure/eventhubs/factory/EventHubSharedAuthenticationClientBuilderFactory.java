// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubProperties;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.integration.eventhub.factory.EventHubSharedAuthenticationClientBuilder;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Event Hub client builder factory, it builds the {@link EventHubClientBuilder} according the configuration context and
 * blob properties.
 */
public class EventHubSharedAuthenticationClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<EventHubSharedAuthenticationClientBuilder> {

    private final AzureEventHubProperties eventHubProperties;

    public EventHubSharedAuthenticationClientBuilderFactory(AzureEventHubProperties eventHubProperties) {
        this.eventHubProperties = eventHubProperties;
    }

    @Override
    protected BiConsumer<EventHubSharedAuthenticationClientBuilder, ProxyOptions> consumeProxyOptions() {
        return EventHubClientBuilder::proxyOptions;
    }

    @Override
    protected BiConsumer<EventHubSharedAuthenticationClientBuilder, AmqpTransportType> consumeAmqpTransportType() {
        return EventHubClientBuilder::transportType;
    }

    @Override
    protected BiConsumer<EventHubSharedAuthenticationClientBuilder, AmqpRetryOptions> consumeAmqpRetryOptions() {
        return EventHubClientBuilder::retry;
    }

    @Override
    protected BiConsumer<EventHubSharedAuthenticationClientBuilder, ClientOptions> consumeClientOptions() {
        return EventHubClientBuilder::clientOptions;
    }

    @Override
    protected BiConsumer<EventHubSharedAuthenticationClientBuilder, Configuration> consumeConfiguration() {
        return EventHubClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<EventHubSharedAuthenticationClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, tokenCredential) -> builder.credential(eventHubProperties.getFQDN(),
                                                                tokenCredential);
    }

    @Override
    protected BiConsumer<EventHubSharedAuthenticationClientBuilder, String> consumeConnectionString() {
        return EventHubClientBuilder::connectionString;
    }

    @Override
    protected EventHubSharedAuthenticationClientBuilder createBuilderInstance() {
        return new EventHubSharedAuthenticationClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.eventHubProperties;
    }

    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>

    @Override
    protected void configureService(EventHubSharedAuthenticationClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(eventHubProperties.getConsumerGroup()).to(builder::consumerGroup);
        map.from(eventHubProperties.getPrefetchCount()).to(builder::prefetchCount);
        map.from(eventHubProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);
        map.from(eventHubProperties.getSharedConnection()).whenTrue().to(t -> builder.shareConnection());
    }


    //Credentials have not been set. They can be set using:
    // connectionString(String),
    // connectionString(String, String),
    // credentials(String, String, TokenCredential),
    // or setting the environment variable 'AZURE_EVENT_HUBS_CONNECTION_STRING' with a connection string
    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(EventHubSharedAuthenticationClientBuilder builder) {
        return Arrays.asList(
            new NamedKeyAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                                provider.getCredential())),
            new SasAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                           provider.getCredential())),
            new TokenAuthenticationDescriptor(provider -> builder.credential(eventHubProperties.getFQDN(),
                                                                             provider.getCredential()))
        );
    }

}
