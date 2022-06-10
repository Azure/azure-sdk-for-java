// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusReceiverClientProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.util.Assert;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder.ServiceBusReceiverClientBuilder}.
 */
public class ServiceBusReceiverClientBuilderFactory
    extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder,
        ServiceBusReceiverClientProperties> {

    private final ServiceBusReceiverClientProperties receiverClientProperties;

    /**
     * Create a {@link ServiceBusReceiverClientBuilderFactory} instance with the {@link ServiceBusReceiverClientProperties}.
     * @param properties the properties of a Service Bus receiver client.
     */
    public ServiceBusReceiverClientBuilderFactory(ServiceBusReceiverClientProperties properties) {
        this(null, properties);
    }

    /**
     * Create a {@link ServiceBusReceiverClientBuilderFactory} instance with {@link ServiceBusClientBuilder} and the
     * {@link ServiceBusReceiverClientProperties}.
     *
     * @param serviceBusClientBuilder the provided Service Bus client builder. If provided, the sub clients will be
     *                                created from this builder.
     * @param properties the properties of the Service Bus receiver client.
     */
    public ServiceBusReceiverClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                  ServiceBusReceiverClientProperties properties) {
        super(serviceBusClientBuilder, properties);
        this.receiverClientProperties = properties;
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusReceiverClientBuilder createBuilderInstance() {
        return this.getServiceBusClientBuilder().receiver();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder) {
        Assert.notNull(receiverClientProperties.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(receiverClientProperties.getEntityName(), "Entity name cannot be null.");
        if (ServiceBusEntityType.TOPIC == receiverClientProperties.getEntityType()) {
            Assert.notNull(receiverClientProperties.getSubscriptionName(), "Subscription cannot be null.");
        }

        final PropertyMapper propertyMapper = new PropertyMapper();
        if (ServiceBusEntityType.QUEUE == receiverClientProperties.getEntityType()) {
            propertyMapper.from(receiverClientProperties.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC == receiverClientProperties.getEntityType()) {
            propertyMapper.from(receiverClientProperties.getEntityName()).to(builder::topicName);
            propertyMapper.from(receiverClientProperties.getSubscriptionName()).to(builder::subscriptionName);
        }

        propertyMapper.from(receiverClientProperties.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(receiverClientProperties.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(receiverClientProperties.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(receiverClientProperties.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(receiverClientProperties.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
    }

}
