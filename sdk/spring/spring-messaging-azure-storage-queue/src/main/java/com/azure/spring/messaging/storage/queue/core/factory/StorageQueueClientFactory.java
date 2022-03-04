// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.storage.queue.core.factory;

import com.azure.storage.queue.QueueAsyncClient;

/**
 * The strategy to produce {@link QueueAsyncClient} instance.
 */
public interface StorageQueueClientFactory {

    /**
     * Create {@link QueueAsyncClient} to send and receive messages to/from Storage Queue.
     * @param queueName the queue name
     * @return the QueueAsyncClient.
     */
    QueueAsyncClient createQueueClient(String queueName);

    /**
     * Add a listener for this factory.
     * @param listener the listener
     */
    default void addListener(Listener listener) {

    }

    /**
     * Remove a listener
     * @param listener the listener
     * @return true if removed.
     */
    default boolean removeListener(Listener listener) {
        return false;
    }

    /**
     * Called whenever a {@link QueueAsyncClient} is added or removed.
     */
    @FunctionalInterface
    interface Listener {

        /**
         * The callback method that the queue client has been added.
         * @param name the name for the queue.
         * @param client the queue client.
         */
        void queueClientAdded(String name, QueueAsyncClient client);

        /**
         * The default callback method that the queue client has been removed.
         * @param name the name for the queue.
         * @param client the queue client.
         */
        default void queueClientRemoved(String name, QueueAsyncClient client) {
        }

    }

}
