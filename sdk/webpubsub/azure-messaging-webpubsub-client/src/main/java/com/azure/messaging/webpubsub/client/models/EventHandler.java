// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

/**
 * The event handler.
 *
 * @param <T> the type of event.
 */
@FunctionalInterface
public interface EventHandler<T> {

    /**
     * Handles the event.
     *
     * @param event the event.
     */
    void handle(T event);
}
