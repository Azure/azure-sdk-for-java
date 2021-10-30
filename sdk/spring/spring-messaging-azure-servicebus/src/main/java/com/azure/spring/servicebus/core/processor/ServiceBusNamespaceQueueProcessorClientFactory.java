// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.processor;


import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.servicebus.core.ServiceBusMessageProcessor;

/**
 * Factory to return functional creator of {@link ServiceBusProcessorClient}.
 *
 * @author Warren Zhu
 */
public interface ServiceBusNamespaceQueueProcessorClientFactory {

    /**
     * Return a function which accepts service bus queue name, then returns {@link ServiceBusProcessorClient}
     *
     * @param name The queue name.
     * @param messageProcessor Callback processor to be registered on service bus processor client.
     * @return ServiceBusProcessorClient queue processor client
     */
    ServiceBusProcessorClient createProcessor(String name,
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

        default void processorAdded(String queue) {

        }

        default void processorRemoved(String queue) {
        }

    }

}
