// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;


import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;

import java.util.function.Consumer;

/**
 * Factory to return functional creator of service bus queue client
 *
 * @author Warren Zhu
 */
public interface ServiceBusQueueClientFactory extends ServiceBusSenderFactory {
    /**
     * Return a function which accepts service bus queue name, then returns {@link ServiceBusProcessorClient}
     * @param name queue name
     * @return ServiceBusProcessorClient queue processor client
     */
    ServiceBusProcessorClient getOrCreateClient(String name, ServiceBusClientConfig clientConfig, Consumer processMessage, Consumer processError);


}
