// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.config;

import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;

/**
 * Called by the binder to customize the {@link ServiceBusProcessorFactory}.
 */
@FunctionalInterface
public interface ServiceBusProcessorFactoryCustomizer {

    /**
     * Customize the Service Bus processor factory.
     *
     * @param factory The Service Bus processor factory.
     */
    void customize(ServiceBusProcessorFactory factory);

}
