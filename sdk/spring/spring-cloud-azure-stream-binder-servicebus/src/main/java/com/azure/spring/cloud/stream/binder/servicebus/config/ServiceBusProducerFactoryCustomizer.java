// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.spring.messaging.servicebus.core.ServiceBusProducerFactory;

/**
 * Called by the binder to customize the {@link ServiceBusProducerFactory}.
 */
@FunctionalInterface
public interface ServiceBusProducerFactoryCustomizer {

    /**
     * Customize the Service Bus producer factory.
     *
     * @param factory The Service Bus producer factory.
     */
    void customize(ServiceBusProducerFactory factory);

}
