// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.implementation.AzureServiceBusJmsConnectionFactoryCustomizer;

/**
 * A customizer for {@link ServiceBusJmsConnectionFactory}.
 *
 * @deprecated Use {@link AzureServiceBusJmsConnectionFactoryCustomizer} instead.
 */
@FunctionalInterface
@Deprecated
public interface ServiceBusJmsConnectionFactoryCustomizer {

    /**
     * Customize the given {@link ServiceBusJmsConnectionFactory}.
     * @param factory The Service Bus JMS connection factory.
     */
    void customize(ServiceBusJmsConnectionFactory factory);
}
