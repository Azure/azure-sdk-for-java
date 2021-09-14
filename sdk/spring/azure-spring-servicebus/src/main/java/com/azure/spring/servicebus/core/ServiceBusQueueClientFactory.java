// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;


import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.servicebus.support.ServiceBusClientConfig;

/**
 * Factory to return functional creator of service bus queue client
 *
 * @author Warren Zhu
 */
public interface ServiceBusQueueClientFactory extends ServiceBusSenderFactory {

    /**
     * Return a function which accepts service bus queue name, then returns {@link ServiceBusProcessorClient}
     *
     * @param name The queue name.
     * @param clientConfig The queue client config.
     * @param messageProcessor Callback processor to be registered on service bus processor client.
     * @return ServiceBusProcessorClient queue processor client
     */
    ServiceBusProcessorClient getOrCreateProcessor(String name,
                                                   ServiceBusClientConfig clientConfig,
                                                   ServiceBusMessageProcessor<ServiceBusReceivedMessageContext,
                                                                                 ServiceBusErrorContext> messageProcessor);


}
