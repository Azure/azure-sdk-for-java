// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusProcessorClientProperties;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder}.
 */
public class ServiceBusSessionProcessorClientBuilderFactory extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder, ServiceBusProcessorClientProperties> {

    private final MessageListener<?> messageListener;
    private final ServiceBusErrorHandler errorHandler;

    /**
     * Create a {@link ServiceBusSessionProcessorClientBuilderFactory} instance with the {@link
     * ServiceBusProcessorClientProperties} and a {@link MessageListener}.
     *
     * @param properties the properties of a Service Bus processor client.
     * @param messageListener the message listener.
     * @param errorHandler the error handler.
     */
    public ServiceBusSessionProcessorClientBuilderFactory(ServiceBusProcessorClientProperties properties,
                                                          List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> serviceBusClientBuilderCustomizers,
                                                          MessageListener<?> messageListener,
                                                          ServiceBusErrorHandler errorHandler) {
        this(null, properties, serviceBusClientBuilderCustomizers, messageListener, errorHandler);
    }

    /**
     * Create a {@link ServiceBusSessionProcessorClientBuilderFactory} instance with the {@link ServiceBusClientBuilder}
     * , the properties and the message processing listener.
     *
     * @param serviceBusClientBuilder the provided Service Bus client builder. If provided, the sub clients will be
     * created from this builder.
     * @param properties the processor client properties.
     * @param processingListener the message listener.
     * @param errorHandler the error handler.
     */
    public ServiceBusSessionProcessorClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                          ServiceBusProcessorClientProperties properties,
                                                          MessageListener<?> processingListener,
                                                          ServiceBusErrorHandler errorHandler) {
        this(serviceBusClientBuilder, properties, null, processingListener, errorHandler);
    }

    private ServiceBusSessionProcessorClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                           ServiceBusProcessorClientProperties properties,
                                                           List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> serviceBusClientBuilderCustomizers,
                                                           MessageListener<?> messageListener,
                                                           ServiceBusErrorHandler errorHandler) {
        super(serviceBusClientBuilder, properties, serviceBusClientBuilderCustomizers);
        this.messageListener = messageListener;
        this.errorHandler = errorHandler;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder createBuilderInstance() {
        return this.getServiceBusClientBuilder().sessionProcessor();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {
        Assert.notNull(properties.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(properties.getEntityName(), "Entity name cannot be null.");
        super.configureService(builder);
        final PropertyMapper propertyMapper = new PropertyMapper();

        if (ServiceBusEntityType.QUEUE == properties.getEntityType()) {
            propertyMapper.from(properties.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC == properties.getEntityType()) {
            propertyMapper.from(properties.getEntityName()).to(builder::topicName);
        }

        propertyMapper.from(properties.getSubscriptionName()).to(builder::subscriptionName);
        propertyMapper.from(properties.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(properties.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(properties.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(properties.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(properties.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
        propertyMapper.from(properties.getMaxConcurrentCalls()).to(builder::maxConcurrentCalls);
        propertyMapper.from(properties.getMaxConcurrentSessions()).to(builder::maxConcurrentSessions);

        propertyMapper.from(this.errorHandler).to(builder::processError);

        configureProcessorListener(builder);
    }

    private void configureProcessorListener(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {
        if (messageListener instanceof ServiceBusRecordMessageListener) {
            builder.processMessage(((ServiceBusRecordMessageListener) messageListener)::onMessage);
        } else {
            throw new IllegalArgumentException("Listener must be a '"
                + ServiceBusRecordMessageListener.class.getSimpleName()
                + "' not " + messageListener.getClass().getName());
        }
    }

}
