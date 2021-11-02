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
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.service.servicebus.properties.ServiceBusProducerDescriptor;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder.ServiceBusSenderClientBuilder}.
 */
public class ServiceBusSenderClientBuilderFactory extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusSenderClientBuilder, ServiceBusProducerDescriptor> {

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
        Assert.notNull(producerProperties.getType(), "Entity type cannot be null.");
        Assert.notNull(producerProperties.getName(), "Entity name cannot be null.");
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (ServiceBusEntityType.QUEUE.equals(producerProperties.getType())) {
            propertyMapper.from(producerProperties.getName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC.equals(producerProperties.getType())) {
            propertyMapper.from(producerProperties.getName()).to(builder::topicName);
        }

    }

}
