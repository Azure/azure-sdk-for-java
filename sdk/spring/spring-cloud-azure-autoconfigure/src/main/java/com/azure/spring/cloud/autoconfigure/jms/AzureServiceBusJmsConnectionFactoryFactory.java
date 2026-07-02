// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;

/**
 * Defines how a {@link ServiceBusJmsConnectionFactory} instance is created.
 * <p>
 * Provide this interface as a Spring bean to customize creation of
 * {@link ServiceBusJmsConnectionFactory} (or a subclass).
 * </p>
 * <p>
 * The factory can be invoked multiple times in one application context
 * (for example sender and listener container paths). Implementations should
 * return a new {@link ServiceBusJmsConnectionFactory} instance per invocation,
 * or otherwise ensure the returned instance is safe to share.
 * </p>
 */
@FunctionalInterface
public interface AzureServiceBusJmsConnectionFactoryFactory {

    /**
     * Creates an instance of {@link ServiceBusJmsConnectionFactory} or a subclass thereof.
     *
     * @return an instance of {@link ServiceBusJmsConnectionFactory}
     */
    ServiceBusJmsConnectionFactory createServiceBusJmsConnectionFactory();
}
