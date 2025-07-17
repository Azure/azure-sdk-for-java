package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;

/**
 * The interface used to define how the {@link ServiceBusJmsConnectionFactory} instance is created.
 */
public interface AzureServiceBusJmsConnectionFactoryFactory {

    /**
     * Creates an instance of {@link ServiceBusJmsConnectionFactory} or a subclass thereof.
     *
     * @return an instance of {@link ServiceBusJmsConnectionFactory}
     */
    ServiceBusJmsConnectionFactory createServiceBusJmsConnectionFactory();
}
