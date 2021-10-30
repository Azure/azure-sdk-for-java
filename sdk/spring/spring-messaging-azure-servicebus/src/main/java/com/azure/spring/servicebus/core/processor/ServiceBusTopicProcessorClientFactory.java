// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;


import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.servicebus.core.ServiceBusMessageProcessor;

/**
 * Factory to return functional creator of service bus topic and subscription client
 *
 * @author Warren Zhu
 */
public interface ServiceBusTopicProcessorClientFactory {
    /**
     * Return a function which accepts service bus topic and subscription name, then returns {@link
     * ServiceBusProcessorClient}
     *
     * @param topic The topic.
     * @param subscription The subscription.
     * @param messageProcessor The callback processor to be registered on service bus processor client.
     * @return subscription client
     */
    ServiceBusProcessorClient createProcessor(String topic,
                                              String subscription,
                                              ServiceBusMessageProcessor messageProcessor);

    default void addListener(Listener listener) {

    }

    default boolean removeListener(Listener listener) {
        return false;
    }

    /**
     * Called whenever a processor is added or removed.
     */
    interface Listener {

        default void processorAdded(String topic, String subscription) {

        }

        default void processorRemoved(String topic, String subscription) {
        }

    }
}
