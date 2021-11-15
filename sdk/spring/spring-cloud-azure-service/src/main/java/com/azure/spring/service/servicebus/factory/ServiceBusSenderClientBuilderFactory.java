// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.service.servicebus.properties.ServiceBusProducerDescriptor;
import org.springframework.util.Assert;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder.ServiceBusSenderClientBuilder}.
 */
public class ServiceBusSenderClientBuilderFactory
    extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusSenderClientBuilder,
    ServiceBusProducerDescriptor> {

    private final ServiceBusProducerDescriptor producerProperties;

    public ServiceBusSenderClientBuilderFactory(ServiceBusProducerDescriptor producerProperties) {
        this(null, producerProperties);
    }

    public ServiceBusSenderClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                ServiceBusProducerDescriptor producerProperties) {
        super(serviceBusClientBuilder, producerProperties);
        this.producerProperties = producerProperties;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusSenderClientBuilder createBuilderInstance() {
        return this.serviceBusClientBuilder.sender();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder) {
        Assert.notNull(producerProperties.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(producerProperties.getEntityName(), "Entity name cannot be null.");
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (ServiceBusEntityType.QUEUE.equals(producerProperties.getEntityType())) {
            propertyMapper.from(producerProperties.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC.equals(producerProperties.getEntityType())) {
            propertyMapper.from(producerProperties.getEntityName()).to(builder::topicName);
        }

    }

}
