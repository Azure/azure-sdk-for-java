// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusSenderClientProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder.ServiceBusSenderClientBuilder}.
 */
public class ServiceBusSenderClientBuilderFactory
    extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusSenderClientBuilder,
        ServiceBusSenderClientProperties> {

    /**
     * Create a {@link ServiceBusSenderClientBuilderFactory} instance with the {@link ServiceBusSenderClientProperties}.
     * @param senderClientProperties the properties of a Service Bus sender client.
     */
    public ServiceBusSenderClientBuilderFactory(ServiceBusSenderClientProperties senderClientProperties,
                                                List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> serviceClientBuilderCustomizers) {
        this(null, senderClientProperties, serviceClientBuilderCustomizers);
    }

    /**
     * Create a {@link ServiceBusSenderClientBuilderFactory} instance with {@link ServiceBusClientBuilder} and the
     * {@link ServiceBusSenderClientProperties}.
     *
     * @param serviceBusClientBuilder the provided Service Bus client builder. If provided, the sub clients will be
     *                                created from this builder.
     * @param senderClientProperties the properties of the Service Bus sender client.
     */
    public ServiceBusSenderClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                ServiceBusSenderClientProperties senderClientProperties) {
        this(serviceBusClientBuilder, senderClientProperties, null);
    }

    private ServiceBusSenderClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                 ServiceBusSenderClientProperties senderClientProperties,
                                                 List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> serviceClientBuilderCustomizers) {
        super(serviceBusClientBuilder, senderClientProperties, serviceClientBuilderCustomizers);
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusSenderClientBuilder createBuilderInstance() {
        return this.getServiceBusClientBuilder().sender();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder) {
        Assert.notNull(properties.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(properties.getEntityName(), "Entity name cannot be null.");
        super.configureService(builder);
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (ServiceBusEntityType.QUEUE == properties.getEntityType()) {
            propertyMapper.from(properties.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC == properties.getEntityType()) {
            propertyMapper.from(properties.getEntityName()).to(builder::topicName);
        }
    }

}
