// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.service.servicebus.properties.ServiceBusProcessorDescriptor;
import org.springframework.util.Assert;

import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.TOPIC;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusProcessorClientBuilderFactory extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder, ServiceBusProcessorDescriptor> {

    private final ServiceBusProcessorDescriptor processorDescriptor;
    private final MessageProcessingListener processingListener;
    public ServiceBusProcessorClientBuilderFactory(ServiceBusProcessorDescriptor processorDescriptor,
                                                   MessageProcessingListener processingListener) {
        this(null, processorDescriptor, processingListener);
    }

    public ServiceBusProcessorClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusProcessorDescriptor processorDescriptor,
                                                   MessageProcessingListener processingListener) {
        super(serviceBusClientBuilder, processorDescriptor);
        this.processorDescriptor = processorDescriptor;
        this.processingListener = processingListener;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusProcessorClientBuilder createBuilderInstance() {
        return this.serviceBusClientBuilder.processor();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {
        Assert.notNull(processorDescriptor.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(processorDescriptor.getEntityName(), "Entity name cannot be null.");
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (TOPIC.equals(processorDescriptor.getEntityType())) {
            Assert.notNull(processorDescriptor.getSubscriptionName(), "Subscription cannot be null.");
        }

        if (ServiceBusEntityType.QUEUE.equals(processorDescriptor.getEntityType())) {
            propertyMapper.from(processorDescriptor.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC.equals(processorDescriptor.getEntityType())) {
            propertyMapper.from(processorDescriptor.getEntityName()).to(builder::topicName);
            propertyMapper.from(processorDescriptor.getSubscriptionName()).to(builder::subscriptionName);
        }

        propertyMapper.from(processorDescriptor.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(processorDescriptor.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(processorDescriptor.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(processorDescriptor.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(processorDescriptor.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
        propertyMapper.from(processorDescriptor.getMaxConcurrentCalls()).to(builder::maxConcurrentCalls);

        configureProcessorListener(builder);
    }

    private void configureProcessorListener(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {
        if (processingListener instanceof RecordMessageProcessingListener) {
            builder.processMessage(((RecordMessageProcessingListener) processingListener)::onMessage);
        } else {
            throw new IllegalArgumentException("A " + RecordMessageProcessingListener.class.getSimpleName()
                + " is required when configure record processor.");
        }
        builder.processError(processingListener.getErrorContextConsumer());
    }
}
