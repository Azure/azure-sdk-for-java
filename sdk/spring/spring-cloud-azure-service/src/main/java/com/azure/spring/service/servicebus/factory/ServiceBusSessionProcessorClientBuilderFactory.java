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

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusSessionProcessorClientBuilderFactory extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder, ServiceBusProcessorDescriptor> {

    private final ServiceBusProcessorDescriptor processorDescriptor;
    private final MessageProcessingListener processingListener;

    public ServiceBusSessionProcessorClientBuilderFactory(ServiceBusProcessorDescriptor processorDescriptor,
                                                          MessageProcessingListener processingListener) {
        this(null, processorDescriptor, processingListener);
    }

    public ServiceBusSessionProcessorClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                          ServiceBusProcessorDescriptor processorDescriptor,
                                                          MessageProcessingListener processingListener) {
        super(serviceBusClientBuilder, processorDescriptor);
        this.processorDescriptor = processorDescriptor;
        this.processingListener = processingListener;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder createBuilderInstance() {
        return this.serviceBusClientBuilder.sessionProcessor();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {
        Assert.notNull(processorDescriptor.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(processorDescriptor.getEntityName(), "Entity name cannot be null.");
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (ServiceBusEntityType.QUEUE == processorDescriptor.getEntityType()) {
            propertyMapper.from(processorDescriptor.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC == processorDescriptor.getEntityType()) {
            propertyMapper.from(processorDescriptor.getEntityName()).to(builder::topicName);
        }

        propertyMapper.from(processorDescriptor.getSubscriptionName()).to(builder::subscriptionName);
        propertyMapper.from(processorDescriptor.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(processorDescriptor.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(processorDescriptor.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(processorDescriptor.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(processorDescriptor.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
        propertyMapper.from(processorDescriptor.getMaxConcurrentCalls()).to(builder::maxConcurrentCalls);
        propertyMapper.from(processorDescriptor.getMaxConcurrentSessions()).to(builder::maxConcurrentSessions);
        configureProcessorListener(builder);
    }

    private void configureProcessorListener(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {
        if (processingListener instanceof RecordMessageProcessingListener) {
            builder.processMessage(((RecordMessageProcessingListener) processingListener)::onMessage);
        } else {
            throw new IllegalArgumentException("A " + RecordMessageProcessingListener.class.getSimpleName()
                + " is required when configure record processor.");
        }
        builder.processError(processingListener.getErrorContextConsumer());
    }
}
