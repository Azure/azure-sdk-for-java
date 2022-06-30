// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubSharedKeyCredential;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubClientCommonProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubConsumerProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubsNamespaceProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Event Hub client builder factory, it builds the {@link EventHubClientBuilder} according the configuration context and
 * blob properties.
 */
public class EventHubClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<EventHubClientBuilder> {

    public static final Logger LOGGER = LoggerFactory.getLogger(EventHubClientBuilderFactory.class);

    private final EventHubClientCommonProperties eventHubsProperties;

    /**
     * Create a {@link EventHubClientBuilderFactory} instance with a {@link EventHubClientCommonProperties}.
     * @param eventHubsProperties the properties common to an Event Hubs producer or consumer.
     */
    public EventHubClientBuilderFactory(EventHubClientCommonProperties eventHubsProperties) {
        this.eventHubsProperties = eventHubsProperties;
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
        return EventHubClientBuilder::retryOptions;
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
        return (builder, tokenCredential) -> builder.credential(tokenCredential);
    }

    @Override
    protected BiConsumer<EventHubClientBuilder, String> consumeConnectionString() {
        return (builder, connectionString) -> {
            if (StringUtils.hasText(this.eventHubsProperties.getEventHubName())) {
                builder.connectionString(connectionString, this.eventHubsProperties.getEventHubName());
            } else {
                LOGGER.info("The eventhub name is not configured, will call credential method instead of connectionString method.");
                final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
                TokenCredential tokenCredential = getTokenCredential(properties);
                builder.credential(tokenCredential);
            }
        };
    }

    @Override
    protected EventHubClientBuilder createBuilderInstance() {
        return new EventHubClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.eventHubsProperties;
    }


    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>

    @Override
    protected void configureService(EventHubClientBuilder builder) {
        PropertyMapper mapper = new PropertyMapper();

        mapper.from(eventHubsProperties.getCustomEndpointAddress()).to(builder::customEndpointAddress);
        mapper.from(eventHubsProperties.getFullyQualifiedNamespace()).to(builder::fullyQualifiedNamespace);
        mapper.from(eventHubsProperties.getEventHubName()).to(builder::eventHubName);

        if (this.eventHubsProperties instanceof EventHubsNamespaceProperties) {
            mapper.from(((EventHubsNamespaceProperties) this.eventHubsProperties).getSharedConnection())
                  .whenTrue()
                  .to(t -> builder.shareConnection());
        }

        if (this.eventHubsProperties instanceof EventHubConsumerProperties) {
            EventHubConsumerProperties consumerProperties = (EventHubConsumerProperties) this.eventHubsProperties;
            mapper.from(consumerProperties.getConsumerGroup()).to(builder::consumerGroup);
            mapper.from(consumerProperties.getPrefetchCount()).to(builder::prefetchCount);
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
            new NamedKeyAuthenticationDescriptor(builder::credential),
            new SasAuthenticationDescriptor(builder::credential),
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, c -> builder.credential(c))
        );
    }

    private TokenCredential getTokenCredential(ConnectionStringProperties properties) {
        TokenCredential tokenCredential;
        if (properties.getSharedAccessSignature() == null) {
            tokenCredential = new EventHubSharedKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } else {
            tokenCredential = new EventHubSharedKeyCredential(properties.getSharedAccessSignature());
        }
        return tokenCredential;
    }

}
