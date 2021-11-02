// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.service.servicebus.properties.ServiceBusProcessorDescriptor;
import org.springframework.util.Assert;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusProcessorClientBuilderFactory extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder, ServiceBusProcessorDescriptor> {

    private final ServiceBusProcessorDescriptor processorDescriptor;
    private final MessageProcessingListener listener;
    public ServiceBusProcessorClientBuilderFactory(ServiceBusProcessorDescriptor processorDescriptor,
                                                   MessageProcessingListener listener) {
        this(null, processorDescriptor, listener);
    }

    public ServiceBusProcessorClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusProcessorDescriptor processorDescriptor,
                                                   MessageProcessingListener listener) {
        super(processorDescriptor, serviceBusClientBuilder);
        this.processorDescriptor = processorDescriptor;
        this.listener = listener;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusProcessorClientBuilder createBuilderInstance() {
        return this.serviceBusClientBuilder.processor();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {
        Assert.notNull(processorDescriptor.getType(), "Entity type cannot be null.");
        Assert.notNull(processorDescriptor.getName(), "Entity name cannot be null.");
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (ServiceBusEntityType.QUEUE.equals(processorDescriptor.getType())) {
            propertyMapper.from(processorDescriptor.getName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC.equals(processorDescriptor.getType())) {
            propertyMapper.from(processorDescriptor.getName()).to(builder::topicName);
        }

        propertyMapper.from(processorDescriptor.getSubscriptionName()).to(builder::subscriptionName);
        propertyMapper.from(processorDescriptor.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(processorDescriptor.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(processorDescriptor.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(processorDescriptor.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(processorDescriptor.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
        propertyMapper.from(processorDescriptor.getMaxConcurrentCalls()).to(builder::maxConcurrentCalls);
    }
    
}
