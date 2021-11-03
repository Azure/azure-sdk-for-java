// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.servicebus.properties.ServiceBusConsumerDescriptor;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.util.Assert;

import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.TOPIC;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder.ServiceBusReceiverClientBuilder}.
 */
public class ServiceBusReceiverClientBuilderFactory
    extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder,
    ServiceBusConsumerDescriptor> {

    private final ServiceBusConsumerDescriptor consumerProperties;

    public ServiceBusReceiverClientBuilderFactory(ServiceBusConsumerDescriptor consumerDescriptor) {
        this(null, consumerDescriptor);
    }

    public ServiceBusReceiverClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                  ServiceBusConsumerDescriptor consumerDescriptor) {
        super(serviceBusClientBuilder, consumerDescriptor);
        this.consumerProperties = consumerDescriptor;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusReceiverClientBuilder createBuilderInstance() {
        return this.serviceBusClientBuilder.receiver();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder) {
        Assert.notNull(consumerProperties.getType(), "Entity type cannot be null.");
        Assert.notNull(consumerProperties.getName(), "Entity name cannot be null.");
        if (TOPIC.equals(consumerProperties.getType())) {
            Assert.notNull(consumerProperties.getSubscriptionName(), "Subscription cannot be null.");
        }

        final PropertyMapper propertyMapper = new PropertyMapper();
        if (ServiceBusEntityType.QUEUE.equals(consumerProperties.getType())) {
            propertyMapper.from(consumerProperties.getName()).to(builder::queueName);
        } else if (TOPIC.equals(consumerProperties.getType())) {
            propertyMapper.from(consumerProperties.getName()).to(builder::topicName);
            propertyMapper.from(consumerProperties.getSubscriptionName()).to(builder::subscriptionName);
        }

        propertyMapper.from(consumerProperties.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(consumerProperties.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(consumerProperties.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(consumerProperties.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(consumerProperties.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
    }

}
