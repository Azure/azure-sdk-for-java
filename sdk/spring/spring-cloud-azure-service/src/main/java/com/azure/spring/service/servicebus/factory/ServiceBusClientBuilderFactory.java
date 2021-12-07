// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.factory;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.NamedKeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.servicebus.properties.ServiceBusCommonDescriptor;
import com.azure.spring.service.servicebus.properties.ServiceBusNamespaceDescriptor;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusClientBuilderFactory extends AbstractAzureAmqpClientBuilderFactory<ServiceBusClientBuilder> {

    private final ServiceBusCommonDescriptor serviceBusProperties;

    public ServiceBusClientBuilderFactory(ServiceBusCommonDescriptor serviceBusProperties) {
        this.serviceBusProperties = serviceBusProperties;
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
        return this.serviceBusProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(ServiceBusClientBuilder builder) {
        return Arrays.asList(
            new NamedKeyAuthenticationDescriptor(provider -> builder.credential(serviceBusProperties.getFQDN(),
                                                                                provider.getCredential())),
            new SasAuthenticationDescriptor(provider -> builder.credential(serviceBusProperties.getFQDN(),
                                                                           provider.getCredential())),
            new TokenAuthenticationDescriptor(provider -> builder.credential(serviceBusProperties.getFQDN(),
                                                                             provider.getCredential()))
        );
    }

    @Override
    protected void configureService(ServiceBusClientBuilder builder) {
        PropertyMapper mapper = new PropertyMapper();

        if (this.serviceBusProperties instanceof ServiceBusNamespaceDescriptor) {
            mapper.from(((ServiceBusNamespaceDescriptor) this.serviceBusProperties).getCrossEntityTransactions())
                  .whenTrue().to(t -> builder.enableCrossEntityTransactions());
        }
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, Configuration> consumeConfiguration() {
        return ServiceBusClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return (builder, tokenCredential) -> builder.credential(serviceBusProperties.getFQDN(), tokenCredential);
    }

    @Override
    protected BiConsumer<ServiceBusClientBuilder, String> consumeConnectionString() {
        return ServiceBusClientBuilder::connectionString;
    }
}
