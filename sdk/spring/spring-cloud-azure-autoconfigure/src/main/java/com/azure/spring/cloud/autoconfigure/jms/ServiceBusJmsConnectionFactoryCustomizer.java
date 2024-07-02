// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.jms.ServiceBusJmsConnectionFactory;

/**
 * A customizer for {@link ServiceBusJmsConnectionFactory}.
 */
@FunctionalInterface
public interface ServiceBusJmsConnectionFactoryCustomizer {

    /**
     * Customize the given {@link ServiceBusJmsConnectionFactory}.
     * @param factory The Service Bus JMS connection factory.
     */
    void customize(ServiceBusJmsConnectionFactory factory);
}
