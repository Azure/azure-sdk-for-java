// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.implementation.servicebus.properties.ServiceBusProcessorClientProperties;
import com.azure.spring.service.servicebus.processor.ServiceBusMessageListener;
import com.azure.spring.service.servicebus.processor.ServiceBusRecordMessageListener;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.util.Assert;

import java.util.function.Consumer;

import static com.azure.spring.service.servicebus.properties.ServiceBusEntityType.TOPIC;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusProcessorClientBuilderFactory extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder, ServiceBusProcessorClientProperties> {

    private final ServiceBusProcessorClientProperties processorClientProperties;
    private final ServiceBusMessageListener messageListener;
    private final Consumer<ServiceBusErrorContext> errorContextConsumer;

    /**
     * Create a {@link ServiceBusProcessorClientBuilderFactory} instance with the {@link ServiceBusProcessorClientProperties}
     * and a {@link ServiceBusMessageListener}.
     * @param processorClientProperties the properties of a Service Bus processor client.
     * @param messageListener the message processing listener.
     * @param errorContextConsumer the error context consumer.
     */
    public ServiceBusProcessorClientBuilderFactory(ServiceBusProcessorClientProperties processorClientProperties,
                                                   ServiceBusMessageListener messageListener,
                                                   Consumer<ServiceBusErrorContext> errorContextConsumer) {
        this(null, processorClientProperties, messageListener, errorContextConsumer);
    }

    /**
     * Create a {@link ServiceBusProcessorClientBuilderFactory} instance with the {@link ServiceBusClientBuilder}, the
     * properties and the message processing listener.
     * @param serviceBusClientBuilder the provided Service Bus client builder. If provided, the sub clients will be
     *                                created from this builder.
     * @param processorClientProperties the processor client properties.
     * @param messageListener the message processing listener.
     * @param errorContextConsumer the error context consumer.
     */
    public ServiceBusProcessorClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusProcessorClientProperties processorClientProperties,
                                                   ServiceBusMessageListener messageListener,
                                                   Consumer<ServiceBusErrorContext> errorContextConsumer) {
        super(serviceBusClientBuilder, processorClientProperties);
        this.processorClientProperties = processorClientProperties;
        this.messageListener = messageListener;
        this.errorContextConsumer = errorContextConsumer;
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

        propertyMapper.from(this.errorContextConsumer).to(builder::processError);

        configureMessageListener(builder);
    }

    private void configureMessageListener(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {
        if (messageListener instanceof ServiceBusRecordMessageListener) {
            builder.processMessage(((ServiceBusRecordMessageListener) messageListener)::onMessage);
        } else {
            throw new IllegalArgumentException("A " + ServiceBusRecordMessageListener.class.getSimpleName()
                + " is required when configure record processor.");
        }
    }

}
