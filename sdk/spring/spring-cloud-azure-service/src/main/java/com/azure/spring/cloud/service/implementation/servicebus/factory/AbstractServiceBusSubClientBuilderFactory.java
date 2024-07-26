// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Abstract Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder} sub client.
 */
abstract class AbstractServiceBusSubClientBuilderFactory<T, P extends ServiceBusClientCommonProperties> extends AbstractAzureAmqpClientBuilderFactory<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceBusSubClientBuilderFactory.class);

    private final P properties;

    private final ServiceBusClientBuilderFactory clientBuilderFactory;
    private ServiceBusClientBuilder serviceBusClientBuilder;
    private final boolean shareServiceBusClientBuilder;
    private final List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> clientBuilderCustomizers = new ArrayList<>();
    private ServiceConnectionStringProvider<?> connectionStringProvider;
    private String springIdentifier;

    /**
     * Create a {@link AbstractServiceBusSubClientBuilderFactory} instance with the properties.
     * @param properties the properties describing the service bus sub client, which could be a sender, a receiver or
     *                   a processor.
     */
    AbstractServiceBusSubClientBuilderFactory(P properties) {
        this(null, properties);
    }

    /**
     * Create a {@link AbstractServiceBusSubClientBuilderFactory} instance with a {@link ServiceBusClientBuilder} and
     * the properties.
     * @param serviceBusClientBuilder the provided Service Bus client builder. If provided, the sub clients will be created
     *                                from this builder.
     * @param properties the properties describing the service bus sub client, which could be a sender, a receiver or
     *                   a processor.
     */
    AbstractServiceBusSubClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                              P properties) {
        this.properties = properties;
        if (serviceBusClientBuilder != null) {
            this.clientBuilderFactory = null;
            this.serviceBusClientBuilder = serviceBusClientBuilder;
            this.shareServiceBusClientBuilder = true;
        } else {
            this.clientBuilderFactory = new ServiceBusClientBuilderFactory(properties);
            this.shareServiceBusClientBuilder = false;
        }
    }

    protected boolean isShareServiceBusClientBuilder() {
        return shareServiceBusClientBuilder;
    }

    public void addClientBuilderCustomizer(AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder> clientBuilderCustomizer) {
        if (!isShareServiceBusClientBuilder()) {
            this.clientBuilderCustomizers.add(clientBuilderCustomizer);
        }
    }

    @Override
    public void setSpringIdentifier(String springIdentifier) {
        if (!isShareServiceBusClientBuilder()) {
            if (!StringUtils.hasText(springIdentifier)) {
                LOGGER.warn("SpringIdentifier is null or empty, skip for non-shared ServiceBusClientBuilderFactory.");
                return;
            }

            this.springIdentifier = springIdentifier;
        }
    }

    @Override
    public void setDefaultTokenCredential(TokenCredential defaultTokenCredential) {
        if (!isShareServiceBusClientBuilder()) {
            if (defaultTokenCredential != null) {
                this.defaultTokenCredential = defaultTokenCredential;
            } else {
                LOGGER.debug("Will ignore the 'null' default token credential, skip for non-shared ServiceBusClientBuilderFactory.");
            }
        }
    }

    @Override
    public void setConnectionStringProvider(ServiceConnectionStringProvider<?> connectionStringProvider) {
        if (!isShareServiceBusClientBuilder()) {
            this.connectionStringProvider = connectionStringProvider;
        }
    }

    @Override
    public void setTokenCredentialResolver(AzureCredentialResolver<TokenCredential> tokenCredentialResolver) {
        if (!isShareServiceBusClientBuilder()) {
            if (tokenCredentialResolver != null) {
                this.tokenCredentialResolver = tokenCredentialResolver;
            } else {
                LOGGER.debug("Will ignore the 'null' token credential resolver, skip for non-shared ServiceBusClientBuilderFactory..");
            }
        }
    }

    @Override
    protected BiConsumer<T, ProxyOptions> consumeProxyOptions() {
        return (builder, proxy) -> { };
    }

    @Override
    protected BiConsumer<T, AmqpTransportType> consumeAmqpTransportType() {
        return (builder, t) -> { };
    }

    @Override
    protected BiConsumer<T, AmqpRetryOptions> consumeAmqpRetryOptions() {
        return (builder, retry) -> { };
    }

    @Override
    protected BiConsumer<T, ClientOptions> consumeClientOptions() {
        return (builder, client) -> { };
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.properties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(T builder) {
        return Collections.emptyList();
    }

    @Override
    protected BiConsumer<T, Configuration> consumeConfiguration() {
        return (builder, configuration) -> { };
    }

    @Override
    protected BiConsumer<T, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, credential) -> { };
    }

    @Override
    protected BiConsumer<T, String> consumeConnectionString() {
        return (builder, connectionString) -> { };
    }

    @Override
    protected void configureService(T builder) { }

    protected ServiceBusClientBuilder getServiceBusClientBuilder() {
        if (!this.isShareServiceBusClientBuilder() && this.serviceBusClientBuilder == null) {
            LOGGER.debug("Build the non-shared ServiceBusClientBuilder for {}.", this.getClass().getName());
            assert this.clientBuilderFactory != null;
            this.clientBuilderFactory.setSpringIdentifier(this.springIdentifier);
            this.clientBuilderFactory.setDefaultTokenCredential(this.defaultTokenCredential);
            this.clientBuilderFactory.setConnectionStringProvider(this.connectionStringProvider);
            this.clientBuilderFactory.setTokenCredentialResolver(this.tokenCredentialResolver);
            this.clientBuilderCustomizers.forEach(this.clientBuilderFactory::addBuilderCustomizer);
            this.serviceBusClientBuilder = this.clientBuilderFactory.build();
        }
        return this.serviceBusClientBuilder;
    }
}
