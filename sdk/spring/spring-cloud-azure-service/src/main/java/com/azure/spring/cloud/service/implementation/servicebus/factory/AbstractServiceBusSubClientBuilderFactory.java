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
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Abstract Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder} sub client.
 */
abstract class AbstractServiceBusSubClientBuilderFactory<T, P extends ServiceBusClientCommonProperties> extends AbstractAzureAmqpClientBuilderFactory<T> {

    private final P properties;
    private final ServiceBusClientBuilder serviceBusClientBuilder;

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
            this.serviceBusClientBuilder = serviceBusClientBuilder;
        } else {
            this.serviceBusClientBuilder = new ServiceBusClientBuilderFactory(properties).build();
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
        return serviceBusClientBuilder;
    }
}
