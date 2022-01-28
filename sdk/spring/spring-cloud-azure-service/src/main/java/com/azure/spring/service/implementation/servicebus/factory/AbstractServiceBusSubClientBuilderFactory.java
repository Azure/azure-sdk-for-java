// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.factory;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.factory.AbstractAzureAmqpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;

import java.util.function.BiConsumer;

/**
 * Abstract Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder} sub client.
 */
abstract class AbstractServiceBusSubClientBuilderFactory<T, P extends ServiceBusClientCommonProperties> extends AbstractAzureAmqpClientBuilderFactory<T> {

    private final P properties;
    private final ServiceBusClientBuilder serviceBusClientBuilder;
    private final boolean shareServiceBusClientBuilder;

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
            this.shareServiceBusClientBuilder = true;
        } else {
            this.serviceBusClientBuilder = new ServiceBusClientBuilder();
            this.shareServiceBusClientBuilder = false;
        }
    }

    protected boolean isShareServiceBusClientBuilder() {
        return shareServiceBusClientBuilder;
    }

    @Override
    protected BiConsumer<T, ClientOptions> consumeClientOptions() {
        return (builder, client) -> {
            if (!isShareServiceBusClientBuilder()) {
                this.serviceBusClientBuilder.clientOptions(client);
            }
        };
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.properties;
    }

    protected ServiceBusClientBuilder getServiceBusClientBuilder() {
        return serviceBusClientBuilder;
    }
}
