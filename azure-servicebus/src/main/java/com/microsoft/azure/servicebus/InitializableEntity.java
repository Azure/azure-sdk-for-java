// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ClientEntity;

/**
 * Represents an entity that needs to be initialized before using. This class defines a standard way of properly initializing client objects.
 *
 * @since 1.0
 */
abstract class InitializableEntity extends ClientEntity {

    //TODO Init and close semantics are primitive now. Fix them with support for other states like Initializing, Closing, and concurrency.
    protected InitializableEntity(String clientId, ClientEntity parent) {
        super(clientId, parent);
    }

    /**
     * Initializes this object. This method is asynchronous and returns a CompletableFuture immediately. Initializing of the object is complete when the returned future completes.
     *
     * @return CompletableFuture representing the initialization
     */
    abstract CompletableFuture<Void> initializeAsync();

}
