// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubProperties;
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
public class EventHubClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<EventHubClientBuilder> {

    private final AzureEventHubProperties eventHubProperties;

    public EventHubClientBuilderFactory(AzureEventHubProperties eventHubProperties) {
        this.eventHubProperties = eventHubProperties;
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, ProxyOptions> consumeProxyOptions() {
        return EventHubClientBuilder::proxyOptions;
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, AmqpTransportType> consumeAmqpTransportType() {
        return EventHubClientBuilder::transportType;
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, AmqpRetryOptions> consumeAmqpRetryOptions() {
        return EventHubClientBuilder::retry;
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, ClientOptions> consumeClientOptions() {
        return EventHubClientBuilder::clientOptions;
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, Configuration> consumeConfiguration() {
        return EventHubClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, tokenCredential) -> builder.credential(eventHubProperties.getFQDN(),
                                                                eventHubProperties.getEventHubName(),
                                                                tokenCredential);
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, String> consumeConnectionString() {
        return EventHubClientBuilder::connectionString;
    }

    @Override
    protected EventHubClientBuilder createBuilderInstance() {
        return new EventHubClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.eventHubProperties;
    }


    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>

    @Override
    protected void configureService(EventHubClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(eventHubProperties.getConsumerGroup()).to(builder::consumerGroup);
        map.from(eventHubProperties.getPrefetchCount()).to(builder::prefetchCount);
        map.from(eventHubProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);
        if (eventHubProperties.isSharedConnection()) {
            builder.shareConnection();
        }
    }


    //Credentials have not been set. They can be set using:
    // connectionString(String),
    // connectionString(String, String),
    // credentials(String, String, TokenCredential),
    // or setting the environment variable 'AZURE_EVENT_HUBS_CONNECTION_STRING' with a connection string
    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(EventHubClientBuilder builder) {
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
    protected String getApplicationId() {
        return super.getApplicationId();
    }
}
