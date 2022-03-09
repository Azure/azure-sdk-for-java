// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusProcessorClientProperties;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.util.Assert;

import static com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType.TOPIC;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusProcessorClientBuilderFactory extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder, ServiceBusProcessorClientProperties> {

    private final ServiceBusProcessorClientProperties processorClientProperties;
    private final MessageListener<?> messageListener;
    private final ServiceBusErrorHandler errorHandler;

    /**
     * Create a {@link ServiceBusProcessorClientBuilderFactory} instance with the {@link ServiceBusProcessorClientProperties}
     * and a {@link MessageListener}.
     * @param processorClientProperties the properties of a Service Bus processor client.
     * @param messageListener the message processing listener.
     * @param errorHandler the error handler.
     */
    public ServiceBusProcessorClientBuilderFactory(ServiceBusProcessorClientProperties processorClientProperties,
                                                   MessageListener<?> messageListener,
                                                   ServiceBusErrorHandler errorHandler) {
        this(null, processorClientProperties, messageListener, errorHandler);
    }

    /**
     * Create a {@link ServiceBusProcessorClientBuilderFactory} instance with the {@link ServiceBusClientBuilder}, the
     * properties and the message processing listener.
     * @param serviceBusClientBuilder the provided Service Bus client builder. If provided, the sub clients will be
     *                                created from this builder.
     * @param processorClientProperties the processor client properties.
     * @param messageListener the message processing listener.
     * @param errorHandler the error handler.
     */
    public ServiceBusProcessorClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusProcessorClientProperties processorClientProperties,
                                                   MessageListener<?> messageListener,
                                                   ServiceBusErrorHandler errorHandler) {
        super(serviceBusClientBuilder, processorClientProperties);
        this.processorClientProperties = processorClientProperties;
        this.messageListener = messageListener;
        this.errorHandler = errorHandler;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusProcessorClientBuilder createBuilderInstance() {
        return this.getServiceBusClientBuilder().processor();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {
        Assert.notNull(processorClientProperties.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(processorClientProperties.getEntityName(), "Entity name cannot be null.");
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (TOPIC == processorClientProperties.getEntityType()) {
            Assert.notNull(processorClientProperties.getSubscriptionName(), "Subscription cannot be null.");
        }

        if (ServiceBusEntityType.QUEUE == processorClientProperties.getEntityType()) {
            propertyMapper.from(processorClientProperties.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC == processorClientProperties.getEntityType()) {
            propertyMapper.from(processorClientProperties.getEntityName()).to(builder::topicName);
            propertyMapper.from(processorClientProperties.getSubscriptionName()).to(builder::subscriptionName);
        }

        propertyMapper.from(processorClientProperties.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(processorClientProperties.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(processorClientProperties.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(processorClientProperties.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(processorClientProperties.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
        propertyMapper.from(processorClientProperties.getMaxConcurrentCalls()).to(builder::maxConcurrentCalls);

        propertyMapper.from(this.errorHandler).to(builder::processError);

        configureMessageListener(builder);
    }

    private void configureMessageListener(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {
        if (messageListener instanceof ServiceBusRecordMessageListener) {
            builder.processMessage(((ServiceBusRecordMessageListener) messageListener)::onMessage);
        } else {
            throw new IllegalArgumentException("Listener must be a '"
                + ServiceBusRecordMessageListener.class.getSimpleName()
                + "' not " + messageListener.getClass().getName());
        }
    }

}
