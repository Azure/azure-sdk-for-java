// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;

/**
 * A provider for specifying the desired {@link ServiceBusJmsConnectionFactory} class to be used.
 * Implement this interface and define it as a bean to inject a custom subclass of ServiceBusJmsConnectionFactory.
 *
 * @since 6.1.0
 */
@FunctionalInterface
public interface ServiceBusJmsConnectionFactoryClassProvider {

    /**
     * Get the class of the ServiceBusJmsConnectionFactory to be instantiated.
     * The class must extend {@link ServiceBusJmsConnectionFactory} and have the required constructors.
     *
     * @return The class to be used for creating the ServiceBusJmsConnectionFactory instance.
     */
    Class<? extends ServiceBusJmsConnectionFactory> getConnectionFactoryClass();
}
