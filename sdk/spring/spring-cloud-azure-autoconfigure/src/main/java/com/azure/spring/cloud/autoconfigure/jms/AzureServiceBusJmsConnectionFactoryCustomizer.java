package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;

/**
 * A customizer for {@link ServiceBusJmsConnectionFactory}.
 */
@FunctionalInterface
public interface AzureServiceBusJmsConnectionFactoryCustomizer {

    /**
     * Customize the given {@link ServiceBusJmsConnectionFactory}.
     * @param factory The Service Bus JMS connection factory.
     */
    void customize(ServiceBusJmsConnectionFactory factory);
}
