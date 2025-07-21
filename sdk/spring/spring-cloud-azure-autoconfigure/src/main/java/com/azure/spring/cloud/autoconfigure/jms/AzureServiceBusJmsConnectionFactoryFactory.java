// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
