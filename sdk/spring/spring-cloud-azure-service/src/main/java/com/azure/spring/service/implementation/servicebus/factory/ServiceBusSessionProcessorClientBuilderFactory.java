// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusProcessorClientProperties;
import org.springframework.util.Assert;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusSessionProcessorClientBuilderFactory extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder, ServiceBusProcessorClientProperties> {

    private final ServiceBusProcessorClientProperties processorClientProperties;
    private final MessageProcessingListener processingListener;

    /**
     * Create a {@link ServiceBusSessionProcessorClientBuilderFactory} instance with the
     * {@link ServiceBusProcessorClientProperties} and a {@link MessageProcessingListener}.
     * @param properties the properties of a Service Bus processor client.
     * @param processingListener the message processing listener.
     */
    public ServiceBusSessionProcessorClientBuilderFactory(ServiceBusProcessorClientProperties properties,
                                                          MessageProcessingListener processingListener) {
        this(null, properties, processingListener);
    }

    /**
     * Create a {@link ServiceBusSessionProcessorClientBuilderFactory} instance with the {@link ServiceBusClientBuilder}
     * , the properties and the message processing listener.
     * @param serviceBusClientBuilder the provided Service Bus client builder. If provided, the sub clients will be
     *                                created from this builder.
     * @param properties the processor client properties.
     * @param processingListener the message processing listener.
     */
    public ServiceBusSessionProcessorClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                          ServiceBusProcessorClientProperties properties,
                                                          MessageProcessingListener processingListener) {
        super(serviceBusClientBuilder, properties);
        this.processorClientProperties = properties;
        this.processingListener = processingListener;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder createBuilderInstance() {
        return this.getServiceBusClientBuilder().sessionProcessor();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {
        Assert.notNull(processorClientProperties.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(processorClientProperties.getEntityName(), "Entity name cannot be null.");
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (ServiceBusEntityType.QUEUE == processorClientProperties.getEntityType()) {
            propertyMapper.from(processorClientProperties.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC == processorClientProperties.getEntityType()) {
            propertyMapper.from(processorClientProperties.getEntityName()).to(builder::topicName);
        }

        propertyMapper.from(processorClientProperties.getSubscriptionName()).to(builder::subscriptionName);
        propertyMapper.from(processorClientProperties.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(processorClientProperties.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(processorClientProperties.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(processorClientProperties.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(processorClientProperties.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
        propertyMapper.from(processorClientProperties.getMaxConcurrentCalls()).to(builder::maxConcurrentCalls);
        propertyMapper.from(processorClientProperties.getMaxConcurrentSessions()).to(builder::maxConcurrentSessions);
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
