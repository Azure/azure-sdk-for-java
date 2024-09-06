// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusReceiverClientProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Service Bus client builder factory, it builds the {@link ServiceBusClientBuilder.ServiceBusReceiverClientBuilder}.
 */
public class ServiceBusReceiverClientBuilderFactory
    extends AbstractServiceBusSubClientBuilderFactory<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder,
        ServiceBusReceiverClientProperties> {

    /**
     * Create a {@link ServiceBusReceiverClientBuilderFactory} instance with the {@link ServiceBusReceiverClientProperties}.
     * @param properties the properties of a Service Bus receiver client.
     */
    public ServiceBusReceiverClientBuilderFactory(ServiceBusReceiverClientProperties properties,
                                                  List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> serviceBusClientBuilderCustomizers) {
        this(null, properties, serviceBusClientBuilderCustomizers);
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
        this(serviceBusClientBuilder, properties, null);
    }

    private ServiceBusReceiverClientBuilderFactory(ServiceBusClientBuilder serviceBusClientBuilder,
                                                   ServiceBusReceiverClientProperties properties,
                                                   List<AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder>> serviceClientBuilderCustomizers) {
        super(serviceBusClientBuilder, properties, serviceClientBuilderCustomizers);
    }

    @Override
    protected ServiceBusClientBuilder.ServiceBusReceiverClientBuilder createBuilderInstance() {
        return this.getServiceBusClientBuilder().receiver();
    }

    @Override
    protected void configureService(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder builder) {
        Assert.notNull(properties.getEntityType(), "Entity type cannot be null.");
        Assert.notNull(properties.getEntityName(), "Entity name cannot be null.");
        super.configureService(builder);
        if (ServiceBusEntityType.TOPIC == properties.getEntityType()) {
            Assert.notNull(properties.getSubscriptionName(), "Subscription cannot be null.");
        }

        final PropertyMapper propertyMapper = new PropertyMapper();
        if (ServiceBusEntityType.QUEUE == properties.getEntityType()) {
            propertyMapper.from(properties.getEntityName()).to(builder::queueName);
        } else if (ServiceBusEntityType.TOPIC == properties.getEntityType()) {
            propertyMapper.from(properties.getEntityName()).to(builder::topicName);
            propertyMapper.from(properties.getSubscriptionName()).to(builder::subscriptionName);
        }

        propertyMapper.from(properties.getReceiveMode()).to(builder::receiveMode);
        propertyMapper.from(properties.getSubQueue()).to(builder::subQueue);
        propertyMapper.from(properties.getPrefetchCount()).to(builder::prefetchCount);
        propertyMapper.from(properties.getMaxAutoLockRenewDuration()).to(builder::maxAutoLockRenewDuration);
        propertyMapper.from(properties.getAutoComplete()).whenFalse().to(t -> builder.disableAutoComplete());
    }

}
