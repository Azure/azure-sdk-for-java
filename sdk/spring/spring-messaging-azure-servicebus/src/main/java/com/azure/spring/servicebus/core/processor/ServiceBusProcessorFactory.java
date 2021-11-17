// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;


import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;

/**
 * Factory to return functional creator of {@link ServiceBusProcessorClient}.
 *
 * @author Warren Zhu
 */
public interface ServiceBusProcessorFactory {

    /**
     * Return a function which accepts service bus queue name, then returns {@link ServiceBusProcessorClient}
     *
     * @param queue The queue name.
     * @param messageProcessorListener Callback processor listener to be registered on service bus processor client.
     * @return ServiceBusProcessorClient queue processor client
     */
    ServiceBusProcessorClient createProcessor(String queue,
                                              MessageProcessingListener messageProcessorListener);

    /**
     * Return a function which accepts service bus topic and subscription name, then returns {@link
     * ServiceBusProcessorClient}
     *
     * @param topic The topic.
     * @param subscription The subscription.
     * @param messageProcessorListener The callback processor listener to be registered on service bus processor client.
     * @return subscription client
     */
    ServiceBusProcessorClient createProcessor(String topic,
                                              String subscription,
                                              MessageProcessingListener messageProcessorListener);

    default void addListener(Listener listener) {

    }

    default boolean removeListener(Listener listener) {
        return false;
    }

    /**
     * Called whenever a processor is added or removed.
     */
    @FunctionalInterface
    interface Listener {

        void processorAdded(String name, String subscription);

        default void processorRemoved(String name, String subscription) {
        }

    }

}
