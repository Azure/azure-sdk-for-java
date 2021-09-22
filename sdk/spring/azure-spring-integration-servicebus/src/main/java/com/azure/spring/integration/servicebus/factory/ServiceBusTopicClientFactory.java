// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;


import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;

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
                                                   ServiceBusMessageProcessor messageProcessor);
}
