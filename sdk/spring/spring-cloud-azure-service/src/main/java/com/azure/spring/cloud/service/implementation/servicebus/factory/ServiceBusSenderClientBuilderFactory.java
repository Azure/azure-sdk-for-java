// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusSenderClientProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.util.Assert;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder.ServiceBusSenderClientBuilder}.
 */
public class ServiceBusSenderClientBuilderFactory
    extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusSenderClientBuilder,
        ServiceBusSenderClientProperties> {

    private final ServiceBusSenderClientProperties senderClientProperties;

    /**
     * Create a {@link ServiceBusSenderClientBuilderFactory} instance with the {@link ServiceBusSenderClientProperties}.
     * @param senderClientProperties the properties of a Service Bus sender client.
     */
    public ServiceBusSenderClientBuilderFactory(ServiceBusSenderClientProperties senderClientProperties) {
        this(null, senderClientProperties);
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
        super(serviceBusClientBuilder, senderClientProperties);
        this.senderClientProperties = senderClientProperties;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusSenderClientBuilder createBuilderInstance() {
        return this.getServiceBusClientBuilder().sender();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder) {
        Assert.notNull(senderClientProperties.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(senderClientProperties.getEntityName(), "Entity name cannot be null.");
        super.configureService(builder);
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (ServiceBusEntityType.QUEUE == senderClientProperties.getEntityType()) {
            propertyMapper.from(senderClientProperties.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC == senderClientProperties.getEntityType()) {
            propertyMapper.from(senderClientProperties.getEntityName()).to(builder::topicName);
        }

    }

}
