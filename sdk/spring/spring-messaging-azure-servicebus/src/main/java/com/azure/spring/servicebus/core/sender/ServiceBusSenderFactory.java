// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.sender;


import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

/**
 * Factory to return functional creator of service bus sender
 *
 * @author Warren Zhu
 */
public interface ServiceBusSenderFactory {

    /**
     * Return a function which accepts service bus topic or queue name, then returns {@link ServiceBusSenderClient}
     *
     * @param name sender name
     * @return message sender implement instance
     */
    ServiceBusSenderAsyncClient createSender(String name);

    default void addListener(Listener listener) {

    }

    default boolean removeListener(Listener listener) {
        return false;
    }

    /**
     * Called whenever a producer is added or removed.
     */
    interface Listener {

        default void producerAdded(String name) {

        }

        default void producerRemoved(String name) {
        }

    }
}
