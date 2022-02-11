// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import com.azure.spring.servicebus.core.producer.ServiceBusProducerFactory;

/**
 * Called by the binder to customize the factories.
 */
public interface ClientFactoryCustomizer {

    /**
     * Customize the producer factory.
     * @param factory The Service Bus producer factory.
     */
    default void customize(ServiceBusProducerFactory factory) {
    }

    /**
     * Customize the processor factory.
     * @param factory The Service Bus processor factory.
     */
    default void customize(ServiceBusProcessorFactory factory) {
    }

}
