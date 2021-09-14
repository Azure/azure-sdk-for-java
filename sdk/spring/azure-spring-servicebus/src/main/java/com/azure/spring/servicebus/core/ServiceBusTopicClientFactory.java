// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;


import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.servicebus.support.ServiceBusClientConfig;

/**
 * Factory to return functional creator of service bus topic and subscription client
 *
 * @author Warren Zhu
 */
public interface ServiceBusTopicClientFactory extends ServiceBusSenderFactory {
    /**
     * Return a function which accepts service bus topic and subscription name, then returns {@link
     * ServiceBusProcessorClient}
     *
     * @param topic The topic.
     * @param subscription The subscription.
     * @param clientConfig The topic client config.
     * @param messageProcessor The callback processor to be registered on service bus processor client.
     * @return subscription client
     */
    ServiceBusProcessorClient getOrCreateProcessor(String topic,
                                                   String subscription,
                                                   ServiceBusClientConfig clientConfig,
                                                   ServiceBusMessageProcessor<ServiceBusReceivedMessageContext,
                                                                                 ServiceBusErrorContext> messageProcessor);
}
