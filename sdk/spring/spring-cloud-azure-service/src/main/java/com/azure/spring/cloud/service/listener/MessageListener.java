// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.listener;

/**
 * Top level interface for Azure message listeners.
 *
 * @param <T> the type received by the listener.
 */
@FunctionalInterface
public interface MessageListener<T> {

    /**
     * Invoked with message from any Azure messaging service.
     *
     * @param message the message to be processed.
     */
    void onMessage(T message);
}
