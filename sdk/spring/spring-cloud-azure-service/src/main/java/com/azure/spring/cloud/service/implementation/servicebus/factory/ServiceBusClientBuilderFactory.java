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
import com.azure.spring.cloud.core.implementation.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusNamespaceProperties;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<ServiceBusClientBuilder> {

    private final ServiceBusClientCommonProperties clientCommonProperties;

    /**
     * Create a {@link ServiceBusClientBuilderFactory} instance from the {@link ServiceBusClientCommonProperties}.
     * @param serviceBusProperties the properties common to a receiver, a sender, or a processor.
     */
    public ServiceBusClientBuilderFactory(ServiceBusClientCommonProperties serviceBusProperties) {
        this.clientCommonProperties = serviceBusProperties;
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, ProxyOptions> consumeProxyOptions() {
        return ServiceBusClientBuilder::proxyOptions;
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, AmqpTransportType> consumeAmqpTransportType() {
        return ServiceBusClientBuilder::transportType;
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, AmqpRetryOptions> consumeAmqpRetryOptions() {
        return ServiceBusClientBuilder::retryOptions;
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, ClientOptions> consumeClientOptions() {
        return ServiceBusClientBuilder::clientOptions;
    }

    @Override
    protected ServiceBusClientBuilder createBuilderInstance() {
        return new ServiceBusClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.clientCommonProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(ServiceBusClientBuilder builder) {
        return Arrays.asList(
            new NamedKeyAuthenticationDescriptor(builder::credential),
            new SasAuthenticationDescriptor(builder::credential),
            new TokenAuthenticationDescriptor(this.tokenCredentialResolver, builder::credential)
        );
    }

    @Override
    protected void configureService(ServiceBusClientBuilder builder) {
        PropertyMapper mapper = new PropertyMapper();

        mapper.from(this.clientCommonProperties.getFullyQualifiedNamespace()).to(builder::fullyQualifiedNamespace);

        if (this.clientCommonProperties instanceof ServiceBusNamespaceProperties) {
            mapper.from(((ServiceBusNamespaceProperties) this.clientCommonProperties).getCrossEntityTransactions())
                  .whenTrue()
                  .to(t -> builder.enableCrossEntityTransactions());
        }
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, Configuration> consumeConfiguration() {
        return ServiceBusClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, tokenCredential) -> builder.credential(clientCommonProperties.getFullyQualifiedNamespace(),
            tokenCredential);
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, String> consumeConnectionString() {
        return ServiceBusClientBuilder::connectionString;
    }
}
