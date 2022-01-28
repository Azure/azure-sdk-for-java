// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.factory;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusNamespaceProperties;

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
    protected void configureService(ServiceBusClientBuilder builder) {
        PropertyMapper mapper = new PropertyMapper();

        if (this.clientCommonProperties instanceof ServiceBusNamespaceProperties) {
            mapper.from(((ServiceBusNamespaceProperties) this.clientCommonProperties).getCrossEntityTransactions())
                  .whenTrue().to(t -> builder.enableCrossEntityTransactions());
            mapper.from(this.clientCommonProperties.getFullyQualifiedNamespace())
                .to(builder::fullyQualifiedNamespace);
        }
    }
}
